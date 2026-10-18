# Cloud Deployment Guide: Render.com (24/7 Free Live Hosting)

This guide shows how to deploy the JSPM College Chatbot FastAPI backend to Render so that the Android app can access it 24/7 from any network (Wi-Fi, 4G, 5G).

---

## Prerequisites
- A free account on [GitHub.com](https://github.com/)
- A free account on [Render.com](https://render.com/) (Log in using your GitHub account)

---

## Step 1: Push the Code to GitHub

Open a terminal in the project root (`chatbot_app/`) and run:

1. Create a new repository on GitHub:
   - Go to [github.com/new](https://github.com/new)
   - Name it `jspm-college-chatbot`
   - Leave it public or private (both work with Render)
   - Do NOT check "Initialize with README" (we already have a complete repo committed)

2. Link and push your local commits:
   ```bash
   git remote add origin https://github.com/<YOUR-USERNAME>/jspm-college-chatbot.git
   git branch -M main
   git push -u origin main
   ```

---

## Step 2: Deploy on Render.com

1. Go to your [Render Dashboard](https://dashboard.render.com/).
2. Click **New +** (top right) and select **Web Service**.
3. Under "Connect a repository", search for `jspm-college-chatbot` and click **Connect**.
4. Configure the Web Service:
   - **Name:** `jspm-college-chatbot` *(your live URL will be `https://<NAME>.onrender.com`)*
   - **Region:** `Singapore` (fastest for India/Pune users) or `Oregon`
   - **Root Directory:** `backend`
   - **Environment:** `Python 3` (or `Docker`)
   - **Build Command:** `pip install -r requirements.txt && python -m app.ingestion`
   - **Start Command:** `uvicorn app.main:app --host 0.0.0.0 --port $PORT`
   - **Instance Type:** `Free`
5. Click **Create Web Service** at the bottom.

---

## Step 3: Verify the Live Service

Render will build and deploy the container in 2 to 3 minutes.
When the deploy status turns green (**Live**), click your live URL:
`https://jspm-college-chatbot.onrender.com/api/health`

You will see:
```json
{
  "status": "healthy",
  "service": "JSPM Tathawade Campus Chatbot API",
  "version": "1.0.0",
  "indexed_knowledge_chunks": 36,
  "registered_sources": 16,
  "active_discrepancy_records": 4
}
```

---

## Step 4: Connecting the Android APK

### Option A: Via In-App Settings (Instant)
1. Open the app on the phone.
2. Tap the **⚙️ Settings** icon in the top header.
3. Paste your Render URL: `https://jspm-college-chatbot.onrender.com`.
4. Tap **Save & Connect**. The badge will show **🟢 Online (Connected)**.

### Option B: Bake Directly into the Final APK
Set `currentBaseUrl = "https://jspm-college-chatbot.onrender.com"` in `android/app/src/main/java/com/example/jspmchatbot/data/repository/ChatRepository.kt` and rebuild the APK. The client will open the app and connect automatically with zero setup!
