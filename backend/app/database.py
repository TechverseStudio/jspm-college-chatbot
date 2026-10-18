import sqlite3
import os
from pathlib import Path
from typing import Dict, Any, List, Optional
from app.config import settings

def get_connection() -> sqlite3.Connection:
    settings.DATABASE_PATH.parent.mkdir(parents=True, exist_ok=True)
    conn = sqlite3.connect(settings.DATABASE_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    conn = get_connection()
    cursor = conn.cursor()

    # Sources registry table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS sources (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        url TEXT UNIQUE NOT NULL,
        institute TEXT NOT NULL,
        topic TEXT NOT NULL,
        retrieved_date TEXT NOT NULL,
        last_successful_fetch TEXT,
        content_hash TEXT,
        scraping_status TEXT NOT NULL,
        status_code INTEGER,
        approved_for_answering INTEGER DEFAULT 1,
        error_message TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
    """)

    # Documents table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS documents (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        source_id INTEGER,
        institute TEXT NOT NULL,
        category TEXT NOT NULL,
        title TEXT NOT NULL,
        content TEXT NOT NULL,
        url TEXT NOT NULL,
        academic_year TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (source_id) REFERENCES sources (id)
    )
    """)

    # Knowledge chunks table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS knowledge_chunks (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        document_id INTEGER,
        institute TEXT NOT NULL,
        category TEXT NOT NULL,
        section_title TEXT NOT NULL,
        chunk_text TEXT NOT NULL,
        source_url TEXT NOT NULL,
        academic_year TEXT,
        is_seed INTEGER DEFAULT 1,
        has_conflict INTEGER DEFAULT 0,
        keywords TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (document_id) REFERENCES documents (id)
    )
    """)

    # FTS5 Virtual Table for BM25 full text search
    cursor.execute("""
    CREATE VIRTUAL TABLE IF NOT EXISTS knowledge_fts USING fts5(
        section_title,
        chunk_text,
        category,
        institute,
        keywords,
        content='knowledge_chunks',
        content_rowid='id'
    )
    """)

    # Triggers to keep FTS index synchronized
    cursor.execute("""
    CREATE TRIGGER IF NOT EXISTS knowledge_chunks_ai AFTER INSERT ON knowledge_chunks BEGIN
        INSERT INTO knowledge_fts(rowid, section_title, chunk_text, category, institute, keywords)
        VALUES (new.id, new.section_title, new.chunk_text, new.category, new.institute, new.keywords);
    END;
    """)

    cursor.execute("""
    CREATE TRIGGER IF NOT EXISTS knowledge_chunks_ad AFTER DELETE ON knowledge_chunks BEGIN
        INSERT INTO knowledge_fts(knowledge_fts, rowid, section_title, chunk_text, category, institute, keywords)
        VALUES('delete', old.id, old.section_title, old.chunk_text, old.category, old.institute, old.keywords);
    END;
    """)

    cursor.execute("""
    CREATE TRIGGER IF NOT EXISTS knowledge_chunks_au AFTER UPDATE ON knowledge_chunks BEGIN
        INSERT INTO knowledge_fts(knowledge_fts, rowid, section_title, chunk_text, category, institute, keywords)
        VALUES('delete', old.id, old.section_title, old.chunk_text, old.category, old.institute, old.keywords);
        INSERT INTO knowledge_fts(rowid, section_title, chunk_text, category, institute, keywords)
        VALUES (new.id, new.section_title, new.chunk_text, new.category, new.institute, new.keywords);
    END;
    """)

    # Conflicts table
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS conflicts (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        topic TEXT NOT NULL,
        institute TEXT NOT NULL,
        statement_a TEXT NOT NULL,
        source_a TEXT NOT NULL,
        statement_b TEXT NOT NULL,
        source_b TEXT NOT NULL,
        explanation TEXT NOT NULL,
        resolution_guidance TEXT NOT NULL
    )
    """)

    conn.commit()
    conn.close()

if __name__ == "__main__":
    init_db()
    print("Database initialized successfully.")
