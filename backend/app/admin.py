from typing import List, Optional
from fastapi import APIRouter, HTTPException, Header, Depends
from pydantic import BaseModel

from app.config import settings
from app.database import get_connection
from app.schemas import SourceRegistryItem, ConflictItem
from app.scraper import scrape_all_sources

router = APIRouter(prefix="/api/admin", tags=["Admin & Content Management"])

def verify_admin_token(x_admin_token: Optional[str] = Header(None)):
    if not x_admin_token or x_admin_token != settings.ADMIN_API_KEY:
        # Allow default local access if in debug mode, otherwise reject
        if not settings.DEBUG:
            raise HTTPException(status_code=401, detail="Invalid or missing admin security token")
    return True

class RefreshRequest(BaseModel):
    institute: Optional[str] = None
    force_all: bool = False

class ToggleSourceRequest(BaseModel):
    approved: bool

@router.get("/sources", response_model=List[SourceRegistryItem])
def list_sources(status: Optional[str] = None, authenticated: bool = Depends(verify_admin_token)):
    conn = get_connection()
    cursor = conn.cursor()
    
    if status:
        cursor.execute("SELECT * FROM sources WHERE scraping_status = ? ORDER BY id ASC", (status,))
    else:
        cursor.execute("SELECT * FROM sources ORDER BY id ASC")
        
    rows = cursor.fetchall()
    conn.close()

    items = []
    for r in rows:
        items.append(SourceRegistryItem(
            id=r["id"],
            url=r["url"],
            institute=r["institute"],
            topic=r["topic"],
            retrieved_date=r["retrieved_date"],
            last_successful_fetch=r["last_successful_fetch"],
            content_hash=r["content_hash"],
            scraping_status=r["scraping_status"],
            status_code=r["status_code"],
            approved_for_answering=bool(r["approved_for_answering"]),
            error_message=r["error_message"]
        ))
    return items

@router.post("/sources/{source_id}/toggle")
def toggle_source_approval(source_id: int, payload: ToggleSourceRequest, authenticated: bool = Depends(verify_admin_token)):
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT id FROM sources WHERE id = ?", (source_id,))
    row = cursor.fetchone()
    if not row:
        conn.close()
        raise HTTPException(status_code=404, detail="Source not found")

    cursor.execute(
        "UPDATE sources SET approved_for_answering = ? WHERE id = ?",
        (1 if payload.approved else 0, source_id)
    )
    conn.commit()
    conn.close()
    return {"status": "success", "source_id": source_id, "approved": payload.approved}

@router.get("/conflicts", response_model=List[ConflictItem])
def list_conflicts(authenticated: bool = Depends(verify_admin_token)):
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM conflicts ORDER BY id ASC")
    rows = cursor.fetchall()
    conn.close()

    conflicts = []
    for r in rows:
        conflicts.append(ConflictItem(
            id=r["id"],
            topic=r["topic"],
            institute=r["institute"],
            statement_a=r["statement_a"],
            source_a=r["source_a"],
            statement_b=r["statement_b"],
            source_b=r["source_b"],
            explanation=r["explanation"],
            resolution_guidance=r["resolution_guidance"]
        ))
    return conflicts

@router.post("/refresh")
async def trigger_refresh(payload: RefreshRequest, authenticated: bool = Depends(verify_admin_token)):
    """Triggers an async scraping and re-indexing cycle across official sources."""
    results = await scrape_all_sources()
    success_count = sum(1 for r in results if r["scraping_status"] == "SUCCESS")
    failed_count = sum(1 for r in results if r["scraping_status"] in ("FAILED", "ACCESS_RESTRICTED"))
    return {
        "status": "completed",
        "processed_urls": len(results),
        "successful_fetches": success_count,
        "failed_or_restricted_fetches": failed_count,
        "results": [
            {
                "url": r["url"],
                "institute": r["institute"],
                "status": r["scraping_status"],
                "status_code": r["status_code"],
                "error": r["error_message"]
            }
            for r in results
        ]
    }
