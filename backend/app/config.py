import os
import json
from pathlib import Path
from typing import List
from pydantic import BaseModel
from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent.parent
load_dotenv(BASE_DIR / ".env")

class Settings(BaseModel):
    HOST: str = os.getenv("HOST", "0.0.0.0")
    PORT: int = int(os.getenv("PORT", "8000"))
    DEBUG: bool = os.getenv("DEBUG", "true").lower() in ("true", "1", "yes")

    DATABASE_PATH: Path = BASE_DIR / os.getenv("DATABASE_PATH", "data/knowledge.db")
    SEED_DOCUMENT_PATH: Path = BASE_DIR / os.getenv("SEED_DOCUMENT_PATH", "data/JSPM_RSCOE_College_Chatbot_Knowledge_Base.md")

    ADMIN_API_KEY: str = os.getenv("ADMIN_API_KEY", "jspm_admin_secret_key_2026")

    CORS_ORIGINS: List[str] = ["*"]

    # AI Configuration
    AI_PROVIDER: str = os.getenv("AI_PROVIDER", "none").lower() # none, openai, gemini, anthropic, custom
    AI_API_KEY: str = os.getenv("AI_API_KEY", "")
    AI_MODEL: str = os.getenv("AI_MODEL", "gpt-4o-mini")
    AI_BASE_URL: str = os.getenv("AI_BASE_URL", "https://api.openai.com/v1")

    # Scraper Configuration
    SCRAPER_USER_AGENT: str = os.getenv(
        "SCRAPER_USER_AGENT",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    )
    SCRAPER_TIMEOUT_SECONDS: int = int(os.getenv("SCRAPER_TIMEOUT_SECONDS", "15"))

    # Approved official domains
    APPROVED_DOMAINS: List[str] = [
        "jspmrscoe.edu.in",
        "polytechnic.jspmrscoe.edu.in",
        "msbte.ac.in",
        "jspmrscopr.edu.in",
        "jspmrscoed.edu.in",
        "jspmjims.edu.in",
        "jspmbps.edu.in"
    ]

settings = Settings()
