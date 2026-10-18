import pytest
from fastapi.testclient import TestClient
from app.main import app
from app.scraper import is_domain_allowed, compute_hash

client = TestClient(app)

def test_health_endpoint():
    res = client.get("/api/health")
    assert res.status_code == 200
    data = res.json()
    assert data["status"] == "healthy"
    assert data["indexed_knowledge_chunks"] > 0
    assert data["registered_sources"] > 0

def test_quick_questions_endpoint():
    res = client.get("/api/quick-questions")
    assert res.status_code == 200
    questions = res.json()
    assert len(questions) >= 5
    assert any("fee" in q["question"].lower() for q in questions)
    assert any("package" in q["question"].lower() for q in questions)

def test_chat_supported_fee_query():
    payload = {
        "message": "What is the B.Tech first year fee structure for 2026-27?",
        "institute": "RSCOE"
    }
    res = client.post("/api/chat", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert "1,46,000" in data["answer"] or "146000" in data["answer"] or "Fee Schedule" in data["answer"]
    assert len(data["citations"]) > 0
    assert data["fee_breakdown"] is not None
    assert len(data["fee_breakdown"]) > 0

def test_chat_placement_package_discrepancy():
    payload = {
        "message": "What is the highest package at RSCOE and are there any conflicting figures?",
        "institute": "RSCOE"
    }
    res = client.post("/api/chat", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert "33.8" in data["answer"] or "1 Cr" in data["answer"] or "42 LPA" in data["answer"]
    # Verify discrepancy warning is attached
    assert len(data["warnings"]) > 0
    assert any("Discrepancy" in w["title"] or "Highest Salary" in w["title"] for w in data["warnings"])
    assert data["placement_stats"] is not None
    assert len(data["placement_stats"]) >= 5

def test_chat_unsupported_out_of_domain_query():
    payload = {
        "message": "What is the admission cutoff and fee structure for Oxford University in London?",
        "institute": "ALL"
    }
    res = client.post("/api/chat", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert "could not locate verified official documentation" in data["answer"]
    assert "https://jspmrscoe.edu.in/" in data["answer"]

def test_chat_empty_query_validation():
    payload = {"message": "   "}
    res = client.post("/api/chat", json=payload)
    assert res.status_code == 400

def test_offline_data_bundle():
    res = client.get("/api/offline-data")
    assert res.status_code == 200
    bundle = res.json()
    assert "fee_schedule" in bundle
    assert "placement_statistics" in bundle
    assert "official_contacts" in bundle
    assert len(bundle["fee_schedule"]) > 0

def test_admin_sources_and_conflicts():
    # Admin sources
    res = client.get("/api/admin/sources")
    assert res.status_code == 200
    sources = res.json()
    assert len(sources) > 0
    assert any("jspmrscoe.edu.in" in s["url"] for s in sources)

    # Admin conflicts
    res_conf = client.get("/api/admin/conflicts")
    assert res_conf.status_code == 200
    conflicts = res_conf.json()
    assert len(conflicts) > 0

def test_scraper_domain_allowed():
    assert is_domain_allowed("https://jspmrscoe.edu.in/about") is True
    assert is_domain_allowed("https://jspmrscopr.edu.in/") is True
    assert is_domain_allowed("https://jspmjims.edu.in/storage/doc.pdf") is True
    assert is_domain_allowed("https://unrelated-domain.com/hack") is False

def test_compute_hash():
    h1 = compute_hash("test string")
    h2 = compute_hash("test string")
    h3 = compute_hash("different string")
    assert h1 == h2
    assert h1 != h3
