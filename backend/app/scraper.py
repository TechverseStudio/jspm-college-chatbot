import asyncio
import re
import hashlib
from datetime import datetime
from typing import List, Dict, Any, Optional
import httpx
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse

from app.config import settings
from app.database import get_connection

DEFAULT_TARGET_URLS = [
    {"url": "https://jspmrscoe.edu.in/", "institute": "RSCOE", "topic": "Homepage & Overview"},
    {"url": "https://www.jspmrscoe.edu.in/contact-us", "institute": "RSCOE", "topic": "General Contacts"},
    {"url": "https://www.jspmrscoe.edu.in/admissionenquiry/contact-no", "institute": "RSCOE", "topic": "Admission Contacts"},
    {"url": "https://jspmrscoe.edu.in/placements/placementprocessstatics", "institute": "RSCOE", "topic": "Placements & Statistics"},
    {"url": "https://www.jspmrscoe.edu.in/ComputerEngineering/contact-us", "institute": "RSCOE", "topic": "Computer Dept Contacts"},
    {"url": "https://www.jspmrscoe.edu.in/computerscience-and-businesssystems/contact-us", "institute": "RSCOE", "topic": "CSBS Dept Contacts"},
    {"url": "https://www.jspmrscoe.edu.in/eandtc-engineering/contact-us", "institute": "RSCOE", "topic": "E&TC Dept Contacts"},
    {"url": "https://www.jspmrscoe.edu.in/mechanical-engineering/contact-us", "institute": "RSCOE", "topic": "Mechanical Dept Contacts"},
    {"url": "https://www.jspmrscoe.edu.in/mba/contact-us", "institute": "RSCOE", "topic": "MBA Dept Contacts"},
    {"url": "https://www.jspmrscoe.edu.in/dca/contact-us", "institute": "RSCOE", "topic": "MCA & BCA Dept Contacts"},
    {"url": "https://jspmrscopr.edu.in/", "institute": "RSCOPR", "topic": "Pharmacy College Portal"},
    {"url": "https://jspmrscopr.edu.in/ContactUs/20", "institute": "RSCOPR", "topic": "Pharmacy Contacts"},
    {"url": "https://jspmrscoed.edu.in/", "institute": "RSCOED", "topic": "Education College Portal"},
    {"url": "https://jspmjims.edu.in/", "institute": "JIMS", "topic": "Management Institute Portal"},
    {"url": "https://polytechnic.jspmrscoe.edu.in/", "institute": "POLYTECHNIC", "topic": "Polytechnic Official Portal"},
    {"url": "https://www.jspmrscoe.edu.in/Diploma", "institute": "POLYTECHNIC", "topic": "Diploma Courses Page"},
    {"url": "https://msbte.ac.in/", "institute": "POLYTECHNIC", "topic": "MSBTE Official Website"},
    {"url": "https://jspmbps.edu.in/", "institute": "POLYTECHNIC", "topic": "Polytechnic Portal"}
]

def clean_html_content(html: str) -> Tuple_Text:
    soup = BeautifulSoup(html, "html.parser")

    # Remove script, style, nav, footer, header, noscript, svg, form
    for tag in soup(["script", "style", "nav", "footer", "noscript", "svg", "form", "iframe"]):
        tag.decompose()

    # Extract tables into readable markdown format
    for table in soup.find_all("table"):
        rows_text = []
        for tr in table.find_all("tr"):
            cols = [td.get_text(strip=True) for td in tr.find_all(["th", "td"])]
            if cols:
                rows_text.append(" | ".join(cols))
        if rows_text:
            table_md = "\n" + "\n".join(rows_text) + "\n"
            table.replace_with(soup.new_string(table_md))

    title = soup.title.string.strip() if soup.title and soup.title.string else "Official College Page"
    
    # Extract plain text with good paragraph structure
    text = soup.get_text(separator="\n", strip=True)
    
    # Clean multiple blank lines
    text = re.sub(r'\n{3,}', '\n\n', text)
    return title, text

class Tuple_Text:
    pass

def compute_hash(text: str) -> str:
    return hashlib.sha256(text.encode("utf-8")).hexdigest()

def is_domain_allowed(url: str) -> bool:
    parsed = urlparse(url)
    domain = parsed.netloc.lower()
    for allowed in settings.APPROVED_DOMAINS:
        if domain == allowed or domain.endswith("." + allowed):
            return True
    return False

async def fetch_page(client: httpx.AsyncClient, target: Dict[str, Any]) -> Dict[str, Any]:
    url = target["url"]
    institute = target.get("institute", "ALL")
    topic = target.get("topic", "General")
    
    result = {
        "url": url,
        "institute": institute,
        "topic": topic,
        "status_code": None,
        "scraping_status": "FAILED",
        "title": "",
        "content": "",
        "content_hash": None,
        "error_message": None,
        "retrieved_at": datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")
    }

    if not is_domain_allowed(url):
        result["error_message"] = "Domain not in approved college domain whitelist"
        result["scraping_status"] = "ACCESS_RESTRICTED"
        return result

    try:
        response = await client.get(
            url,
            follow_redirects=True,
            timeout=settings.SCRAPER_TIMEOUT_SECONDS
        )
        result["status_code"] = response.status_code

        if response.status_code == 200:
            title, text = clean_html_content(response.text)
            result["title"] = title
            result["content"] = text
            result["content_hash"] = compute_hash(text)
            result["scraping_status"] = "SUCCESS"
        elif response.status_code == 403:
            result["scraping_status"] = "ACCESS_RESTRICTED"
            result["error_message"] = "HTTP 403 Forbidden: College server access restricted (WAF or bot protection)"
        elif response.status_code == 404:
            result["scraping_status"] = "FAILED"
            result["error_message"] = "HTTP 404 Not Found"
        else:
            result["scraping_status"] = "FAILED"
            result["error_message"] = f"HTTP {response.status_code} Error"

    except httpx.TimeoutException:
        result["scraping_status"] = "FAILED"
        result["error_message"] = f"Timeout after {settings.SCRAPER_TIMEOUT_SECONDS}s"
    except Exception as e:
        result["scraping_status"] = "FAILED"
        result["error_message"] = f"{type(e).__name__}: {str(e)}"

    return result

async def scrape_all_sources(targets: Optional[List[Dict[str, Any]]] = None) -> List[Dict[str, Any]]:
    if not targets:
        targets = DEFAULT_TARGET_URLS

    headers = {
        "User-Agent": settings.SCRAPER_USER_AGENT,
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9"
    }

    results = []
    # Use limits to avoid slamming college servers
    limits = httpx.Limits(max_connections=3, max_keepalive_connections=2)
    async with httpx.AsyncClient(headers=headers, verify=False, limits=limits) as client:
        for t in targets:
            res = await fetch_page(client, t)
            results.append(res)
            # Small courteous delay between requests
            await asyncio.sleep(0.5)

    # Update database
    conn = get_connection()
    cursor = conn.cursor()

    for r in results:
        # Check existing source
        cursor.execute("SELECT id, content_hash FROM sources WHERE url = ?", (r["url"],))
        existing = cursor.fetchone()

        if existing:
            source_id = existing["id"]
            old_hash = existing["content_hash"]
            
            # Update source record
            if r["scraping_status"] == "SUCCESS":
                cursor.execute("""
                UPDATE sources SET
                    topic = ?,
                    retrieved_date = ?,
                    last_successful_fetch = ?,
                    content_hash = ?,
                    scraping_status = ?,
                    status_code = ?,
                    error_message = NULL
                WHERE id = ?
                """, (
                    r["topic"], r["retrieved_at"], r["retrieved_at"],
                    r["content_hash"], r["scraping_status"], r["status_code"], source_id
                ))

                # If content changed, update document & chunks
                if old_hash != r["content_hash"] and len(r["content"]) > 100:
                    cursor.execute("""
                    INSERT INTO documents (source_id, institute, category, title, content, url, academic_year)
                    VALUES (?, ?, 'scraped_page', ?, ?, ?, '2026-27')
                    """, (source_id, r["institute"], r["title"], r["content"], r["url"]))
                    new_doc_id = cursor.lastrowid

                    # Add updated knowledge chunk
                    snippet = r["content"][:2500]
                    cursor.execute("""
                    INSERT INTO knowledge_chunks (
                        document_id, institute, category, section_title, chunk_text,
                        source_url, academic_year, is_seed, has_conflict, keywords
                    ) VALUES (?, ?, 'scraped_update', ?, ?, ?, '2026-27', 0, 0, ?)
                    """, (new_doc_id, r["institute"], r["title"], snippet, r["url"], r["topic"]))
            else:
                cursor.execute("""
                UPDATE sources SET
                    retrieved_date = ?,
                    scraping_status = ?,
                    status_code = ?,
                    error_message = ?
                WHERE id = ?
                """, (
                    r["retrieved_at"], r["scraping_status"], r["status_code"],
                    r["error_message"], source_id
                ))
        else:
            # Insert new source
            cursor.execute("""
            INSERT INTO sources (
                url, institute, topic, retrieved_date, last_successful_fetch,
                content_hash, scraping_status, status_code, approved_for_answering, error_message
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?)
            """, (
                r["url"], r["institute"], r["topic"], r["retrieved_at"],
                r["retrieved_at"] if r["scraping_status"] == "SUCCESS" else None,
                r["content_hash"], r["scraping_status"], r["status_code"],
                r["error_message"]
            ))

    conn.commit()
    conn.close()
    return results

if __name__ == "__main__":
    results = asyncio.run(scrape_all_sources())
    print(f"Scraping completed. Processed {len(results)} URLs.")
    for res in results:
        print(f"[{res['scraping_status']}] {res['url']} (Code: {res['status_code']})")
