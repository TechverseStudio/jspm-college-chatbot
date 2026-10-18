import sys
import json
sys.stdout.reconfigure(encoding='utf-8')
from app.retrieval import retrieve_relevant_evidence
from app.generator import generate_retrieval_only_answer

test_cases = [
    ("Courses & Departments", "What courses and departments are offered at RSCOE and other institutes?", "ALL"),
    ("Current Fees & Schedule", "What are the first-year B.Tech fees for Open, OBC, and SC/ST categories for 2026-27?", "RSCOE"),
    ("Cutoffs (MHT-CET/JEE)", "What is the cutoff score for Computer Engineering at RSCOE?", "RSCOE"),
    ("DSE/DSY Admission", "What is the Direct Second Year admission eligibility, fees, and contacts?", "RSCOE"),
    ("Placement & Conflicting Figures", "What is the highest package at RSCOE and is there any conflicting data?", "RSCOE"),
    ("Recruiters & Internships", "Which prominent recruiters visit RSCOE and what is the internship policy?", "RSCOE"),
    ("Hostel & Transport Details", "Who are the official contacts for hostel facilities and college bus transport?", "RSCOE"),
    ("Scholarships (MahaDBT)", "How do scholarships and fee concessions work for category students?", "ALL"),
    ("Department HODs & Contacts", "Who are the HODs for Computer Engineering, E&TC, and Mechanical departments?", "RSCOE"),
    ("Pharmacy Courses & Admissions", "What courses are offered at Rajarshi Shahu College of Pharmacy?", "RSCOPR"),
    ("Management (JIMS) Details", "What are the intake and courses for JIMS management studies?", "JIMS"),
    ("Unsupported Out-of-Domain", "What is the fee structure for Oxford University in the UK?", "ALL"),
    ("Long Response Formatting", "Give me a complete overview of RSCOE placements, fees, and admissions.", "RSCOE")
]

def run_tests():
    print("=" * 80)
    print("COMPREHENSIVE KNOWLEDGE BASE RETRIEVAL & FACTUAL ACCURACY VERIFICATION")
    print("=" * 80)

    for category, q, inst in test_cases:
        ret = retrieve_relevant_evidence(q, institute=inst)
        ans = generate_retrieval_only_answer(q, ret, inst)
        print(f"\n--- [TEST: {category}] ---")
        print(f"Query: \"{q}\" (Institute: {inst})")
        print(f"Confidence: {ret['confidence']}")
        print(f"Citations ({len(ret['citations'])}): {[c.url for c in ret['citations']]}")
        print(f"Warnings ({len(ret['warnings'])}): {[w.title for w in ret['warnings']]}")
        print(f"Fee Breakdown Attached: {ret['fee_breakdown'] is not None}")
        print(f"Placement Stats Attached: {ret['placement_stats'] is not None}")
        print(f"Contact Cards Attached: {ret['contact_cards'] is not None}")
        print(f"Response Excerpt:\n{ans[:250]}...\n")

if __name__ == "__main__":
    run_tests()
