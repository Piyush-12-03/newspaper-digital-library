# Deploy Newspaper Digital Library to Render

Simple step-by-step guide to deploy your Spring Boot application to Render.

## Prerequisites

✅ Your database is already on Render (PostgreSQL)  
✅ AWS S3 bucket exists: `e-newspaper-library-dev`  
✅ Application runs locally

---

## Step 1: Push Code to GitHub

If not already done:

```bash
git add .
git commit -m "Prepare for Render deployment"
git push origin main
```

---

## Step 2: Create Web Service on Render

1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub repository
4. Configure:
   - **Name**: `newspaper-digital-library`
   - **Region**: Same as your database (probably `Oregon` or `Ohio`)
   - **Branch**: `main`
   - **Runtime**: `Docker`
   - **Instance Type**: `Free` (or `Starter` for better performance)

---

## Step 3: Configure Environment Variables

In Render, go to **Environment** tab and add these variables:

### Required Variables

| Variable | Value | Notes |
|----------|-------|-------|
| `SPRING_PROFILES_ACTIVE` | `prod` | Use production profile |
| `DB_URL` | `jdbc:postgresql://dpg-da2a3av40ujc73b7sj80-a.virginia-postgres.render.com:5432/newspaper_library` | Your Render DB URL (add `jdbc:postgresql://` prefix) |
| `DB_USERNAME` | `newspaper_library_user` | Your database username |
| `DB_PASSWORD` | `hDfgxqRQmVqY5zv3OTFCRxCIJXIlPwsk` | Your database password |
| `JWT_SECRET` | (generate a new one - see below) | **IMPORTANT: Generate secure secret** |
| `AWS_ACCESS_KEY_ID` | (your AWS key) | From your AWS credentials |
| `AWS_SECRET_ACCESS_KEY` | (your AWS secret) | From your AWS credentials |
| `AWS_S3_BUCKET` | `e-newspaper-library-local` | Your S3 bucket name |
| `AWS_REGION` | `us-east-1` | Your S3 region |

### Generate JWT Secret

Run this in PowerShell:

```powershell
$bytes = New-Object byte[] 32
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

Copy the output and use it as `JWT_SECRET`.

---

## Step 4: Deploy

1. Click **"Create Web Service"**
2. Render will automatically:
   - Build Docker image
   - Run Liquibase migrations
   - Start your application
3. Wait 5-10 minutes for first deployment

---

## Step 5: Verify Deployment

Once deployed, you'll get a URL like: `https://newspaper-digital-library.onrender.com`

### Test Endpoints

```bash
# Health check
curl https://newspaper-digital-library.onrender.com/actuator/health

# Get editions
curl https://newspaper-digital-library.onrender.com/api/v1/editions

# Login
curl -X POST https://newspaper-digital-library.onrender.com/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'
```

### Access Swagger UI

Visit: `https://newspaper-digital-library.onrender.com/api/v1/swagger-ui.html`

---

## Step 6: Update S3 CORS (if needed)

If you plan to access from a web frontend:

1. Go to AWS S3 Console
2. Select bucket `e-newspaper-library-local`
3. Go to **Permissions** → **CORS**
4. Add:

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["https://newspaper-digital-library.onrender.com"],
    "ExposeHeaders": ["ETag"]
  }
]
```

---

## Troubleshooting

### Build Fails

- Check **Logs** tab in Render
- Ensure `mvn clean package` works locally
- Check Dockerfile syntax

### Application Won't Start

- Verify environment variables are set correctly
- Check database URL format: `jdbc:postgresql://HOST:5432/DB_NAME`
- Ensure database is accessible (not paused)

### Database Connection Issues

- Render Free databases pause after inactivity
- Upgrade to paid plan or restart database
- Check database IP whitelist (Render auto-whitelists)

### AWS S3 Access Denied

- Verify AWS credentials are correct
- Check IAM policy allows S3 operations
- Test credentials locally first

---

## Important Notes

### Free Tier Limitations

- **Web Service**: Spins down after 15 minutes of inactivity
- **Database**: Pauses after inactivity (paid plans don't pause)
- **Cold Start**: First request after spin down takes 30-60 seconds

### Recommended Upgrades

For production use:

- **Web Service**: Upgrade to `Starter` ($7/month) - No spin down
- **Database**: Upgrade to paid plan ($7/month) - No pausing, 24/7 availability

---

## Post-Deployment Checklist

- [ ] Application health check passes
- [ ] Can login with testuser credentials
- [ ] Swagger UI accessible
- [ ] Database migrations completed (check logs)
- [ ] Can upload PDF (test with small file)
- [ ] S3 upload works (check AWS S3 console)
- [ ] Update frontend API base URL (if you have one)

---

## Update Deployment

Any push to `main` branch will auto-deploy:

```bash
git add .
git commit -m "Update feature"
git push origin main
```

Render will rebuild and redeploy automatically.

---

## Useful Commands

### View Render Logs

```bash
# Install Render CLI
npm install -g render-cli

# Login
render login

# Tail logs
render logs --service newspaper-digital-library --tail
```

### Restart Service

From Render Dashboard: **Manual Deploy** → **Deploy latest commit**

---

## Support

- [Render Documentation](https://render.com/docs)
- [Render Community](https://community.render.com/)
- Your application logs: Check Render Dashboard → Logs tab

---

**You're all set!** 🚀
