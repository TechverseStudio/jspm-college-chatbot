from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field

class PlacementStat(BaseModel):
    academic_year: str
    companies_visited: str
    students_placed: str
    offers: str
    max_salary: str

class FeeItem(BaseModel):
    programme: str
    category: str
    tuition_fee: str
    development_fee: str
    university_fee: str
    total_fee: str

class ContactCard(BaseModel):
    name: str
    role: str
    institute: str
    phone: Optional[str] = None
    email: Optional[str] = None
    department: Optional[str] = None
    source_url: Optional[str] = None

class SourceCitation(BaseModel):
    title: str
    url: str
    institute: str
    retrieved_date: Optional[str] = None
    status: str = "VERIFIED"

class WarningCallout(BaseModel):
    title: str
    message: str
    severity: str = "warning" # info, warning, conflict
    sources: List[str] = []

class ChatRequest(BaseModel):
    message: str
    institute: Optional[str] = "ALL" # ALL, RSCOE, RSCOPR, RSCOED, JIMS, POLYTECHNIC
    session_id: Optional[str] = "default"

class ChatResponse(BaseModel):
    answer: str
    institute: str
    confidence: float
    citations: List[SourceCitation] = []
    warnings: List[WarningCallout] = []
    placement_stats: Optional[List[PlacementStat]] = None
    fee_breakdown: Optional[List[FeeItem]] = None
    contact_cards: Optional[List[ContactCard]] = None
    answer_mode: str = "retrieval_only" # retrieval_only or ai_assisted

class SourceRegistryItem(BaseModel):
    id: int
    url: str
    institute: str
    topic: str
    retrieved_date: str
    last_successful_fetch: Optional[str] = None
    content_hash: Optional[str] = None
    scraping_status: str # SUCCESS, FAILED, ACCESS_RESTRICTED, SEED_PRESERVED
    status_code: Optional[int] = None
    approved_for_answering: bool
    error_message: Optional[str] = None

class ConflictItem(BaseModel):
    id: int
    topic: str
    institute: str
    statement_a: str
    source_a: str
    statement_b: str
    source_b: str
    explanation: str
    resolution_guidance: str

class QuickQuestionItem(BaseModel):
    id: str
    category: str
    question: str
    institute: str
