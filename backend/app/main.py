from contextlib import asynccontextmanager
from typing import List, Dict, Any, Optional
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.database import init_db, get_connection
from app.ingestion import ingest_seed_data
from app.retrieval import (
    retrieve_relevant_evidence, FEE_SCHEDULE, PLACEMENT_STATISTICS, OFFICIAL_CONTACTS
)
from app.generator import generate_retrieval_only_answer, generate_ai_answer
from app.schemas import (
    ChatRequest, ChatResponse, QuickQuestionItem
)
from app.admin import router as admin_router

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize DB and ensure seed knowledge base is ingested
    init_db()
    ingest_seed_data(force=False)
    yield

app = FastAPI(
    title="JSPM Tathawade Campus Chatbot API",
    description="Official Knowledge Retrieval & Website Ingestion API for JSPM College Chatbot Android App",
    version="1.0.0",
    lifespan=lifespan
)

# Enable CORS for mobile emulator, devices, and web harnesses
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount Admin Router
app.include_router(admin_router)

QUICK_QUESTIONS = [
    QuickQuestionItem(
        id="q1",
        category="Fees",
        question="What is the 2026-27 fee structure for first year B.Tech and DSY?",
        institute="RSCOE"
    ),
    QuickQuestionItem(
        id="q2",
        category="Placements",
        question="What is the highest package and placement record at RSCOE?",
        institute="RSCOE"
    ),
    QuickQuestionItem(
        id="q3",
        category="Cutoffs",
        question="What are the MHT-CET and JEE Main cutoffs for Computer Engineering?",
        institute="RSCOE"
    ),
    QuickQuestionItem(
        id="q4",
        category="DSY Admissions",
        question="What is the Direct Second Year (DSY) engineering admission process and contacts?",
        institute="RSCOE"
    ),
    QuickQuestionItem(
        id="q5",
        category="Hostel & Bus",
        question="Who are the official contacts for boys/girls hostel and college bus transport?",
        institute="RSCOE"
    ),
    QuickQuestionItem(
        id="q6",
        category="HOD Contacts",
        question="What are the contact details for Computer, E&TC, and Mechanical department HODs?",
        institute="RSCOE"
    ),
    QuickQuestionItem(
        id="q7",
        category="Pharmacy",
        question="What courses and admission contacts are available for Rajarshi Shahu College of Pharmacy?",
        institute="RSCOPR"
    ),
    QuickQuestionItem(
        id="q8",
        category="Management",
        question="What are the intake and details for MBA and MCA at Jayawant Institute of Management Studies (JIMS)?",
        institute="JIMS"
    ),
    QuickQuestionItem(
        id="q9",
        category="Scholarships",
        question="How can students apply for Maharashtra State (MahaDBT) fee concessions and scholarships?",
        institute="ALL"
    ),
    QuickQuestionItem(
        id="q10",
        category="Polytechnic E&TC",
        question="Who are the staff members and class strength for E&TC (TYEJ) in Polytechnic?",
        institute="POLYTECHNIC"
    ),
    QuickQuestionItem(
        id="q11",
        category="Polytechnic Computer",
        question="What are the divisions and class strengths of Computer Engineering in Polytechnic?",
        institute="POLYTECHNIC"
    ),
    QuickQuestionItem(
        id="q12",
        category="Timetable",
        question="What is the official academic timetable and faculty schedule for TYCO-A?",
        institute="POLYTECHNIC"
    )
]

@app.get("/")
def root():
    return {
        "message": "Chatbot API is running",
        "docs": "/docs"
    }

@app.get("/api/health")
def health_check():
    conn = get_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) as chunk_count FROM knowledge_chunks")
    chunk_count = cursor.fetchone()["chunk_count"]

    cursor.execute("SELECT COUNT(*) as source_count FROM sources")
    source_count = cursor.fetchone()["source_count"]

    cursor.execute("SELECT COUNT(*) as conflict_count FROM conflicts")
    conflict_count = cursor.fetchone()["conflict_count"]
    conn.close()

    return {
        "status": "healthy",
        "service": "JSPM Tathawade Campus Chatbot API",
        "version": "1.0.0",
        "ai_provider": settings.AI_PROVIDER,
        "indexed_knowledge_chunks": chunk_count,
        "registered_sources": source_count,
        "active_discrepancy_records": conflict_count
    }

@app.get("/api/quick-questions", response_model=List[QuickQuestionItem])
def get_quick_questions(institute: Optional[str] = "ALL"):
    if institute and institute != "ALL":
        return [q for q in QUICK_QUESTIONS if q.institute in (institute, "ALL")]
    return QUICK_QUESTIONS

@app.post("/api/chat", response_model=ChatResponse)
async def chat(request: ChatRequest):
    query = request.message.strip()
    if not query:
        raise HTTPException(status_code=400, detail="Query message cannot be empty")

    institute = request.institute or "ALL"

    # Retrieve evidence from SQLite FTS5 index
    retrieval = retrieve_relevant_evidence(query, institute=institute, limit=4)

    # Generate answer
    if settings.AI_PROVIDER != "none" and settings.AI_API_KEY:
        answer = await generate_ai_answer(query, retrieval, institute)
        mode = "ai_assisted"
    else:
        answer = generate_retrieval_only_answer(query, retrieval, institute)
        mode = "retrieval_only"

    return ChatResponse(
        answer=answer,
        institute=institute,
        confidence=retrieval["confidence"],
        citations=retrieval["citations"],
        warnings=retrieval["warnings"],
        placement_stats=retrieval["placement_stats"],
        fee_breakdown=retrieval["fee_breakdown"],
        contact_cards=retrieval["contact_cards"],
        answer_mode=mode
    )

@app.get("/api/offline-data")
def get_offline_data_bundle():
    """Provides bundled knowledge dataset for Android app local fallback."""
    return {
        "generated_at": "2026-10-18",
        "version": "1.0",
        "fee_schedule": [f.model_dump() for f in FEE_SCHEDULE],
        "placement_statistics": [p.model_dump() for p in PLACEMENT_STATISTICS],
        "official_contacts": [c.model_dump() for c in OFFICIAL_CONTACTS],
        "quick_questions": [q.model_dump() for q in QUICK_QUESTIONS]
    }
