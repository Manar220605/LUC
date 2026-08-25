# LUC — Project State

> Short notes on where the project stands, what is decided, and what is still open.
> `IMPLEMENTATION.md` is the build guide. This file is the running status.

Last updated: 2026-08-25

---

## 1. What is already built

| Feature | Notes |
|---|---|
| Search | Postgres full-text search (`tsvector` + GIN index + sync trigger, migration 008). Title matches rank above body matches (`setweight`, migration 016). Search box in header, `/search` page. |
| Accepted answer | Migration 009. Question author marks one answer. It is pinned to the top. |
| Markdown | `react-markdown` + `remark-gfm` + `rehype-sanitize` in `MarkdownContent`. |
| Notifications | Migration 010. Types include answers, upvotes, mentions, mentorship, and `NEW_QUESTION_IN_COMMUNITY`. Bell icon with unread count, `/notifications` page, polling. |
| Notification rules | Migration 011. Mentions also fire on edit but never notify the same person twice for the same content. Every new upvote notifies, on questions and answers (the old "10 upvotes" rule is gone). |
| Student sign-up | FS1-style. Personal email → one code → code + password on one page. Mock CSV: `seed/student-directory.csv`. Forgot email by file number (masked). |
| Public profile | `/profile/[id]`. Anyone can open it. Shows name, photo or initials, bio, badge, join date, score, questions, answers. Alumni (if public): LinkedIn, job, company, degree, faculty, major, grad year. Names on the feed and questions link here. Email and file number stay private. |
| Edit profile | `/profile`. Change display name and bio. Upload or remove a photo (JPEG/PNG/WebP, max 2 MB). Photos are stored on the backend (`/api/uploads/avatars/...`). |
| Alumni directory | `/alumni`. Public list of verified alumni with a public profile. Filters: name/job/major, company, graduation year, faculty. Each row opens `/profile/[id]`. Students can ask for mentorship from a profile. |
| Mentorship requests | Student opens an alumnus profile, sends a short message. Alumnus accepts or declines on `/mentorship`. Both get a notification. No private chat. |
| Follow community | Signed-in users follow a community on `/c/...`. New questions notify followers (not the author). List: `/following`. |
| Save question | Signed-in users save a question from the question page. Private list: `/saved`. Migration 019. |

## 2. Still missing from the first plan

- Tests (Step 14 of `IMPLEMENTATION.md`) — nothing is tested yet.
- Admin navigation menu.

---

## 3. Student auth road (FS1-style) — APPROVED AND BUILT

Copy the Faculty of Sciences I portal (`https://students-fs1.ul.edu.lb`). Do **not** connect to that site. Same steps, our mock CSV.

Modelled on real LU practice: in first year the student registers with a **personal email**. That email is the login. After they prove they own it, file number / faculty / major / courses are already filled. They never type those fields.

### Screens

**A. Sign up** (`/auth/signup`)  
1. One field: personal email from first-year registration.  
2. If that email is in `student_directory` and not already used, send **one** code (6 characters).  
3. Same generic message if the email is unknown (do not say “not found”).

**B. Activate** (`/auth/signup` step 2, like FS1 `/activate-account`)  
1. Code + password + confirm password.  
2. Show read-only name, file number, faculty, major (filled from the directory).  
3. Create Keycloak user and **set that password now**. No second email. No Keycloak “click this link”.  
4. Then they can sign in.

**C. Sign in**  
Personal email + password. Profile already filled.

**D. Forgot email** (`/auth/forgot-email`)  
1. File number only.  
2. If found: show **masked** email, e.g. `jad.m*****r@gmail.com`.  
3. If not found: a calm message, no extra details.  
4. Rate-limit so nobody scans every file number.

**E. Forgot password**  
Keep Keycloak’s existing reset. They already know their email.

### Mock data

- File: `backend/src/main/resources/seed/student-directory.csv`  
- Loaded by `DataSeeder` (dev only) into table `student_directory`.  
- Columns: `file_number,email,first_name,last_name,enrollment_year,faculty,major`  
- `email` = **personal email** (login key), not `@st.ul.edu.lb`. Current mock rows use `@gmail.com` placeholders.  
- One row may use a real Gmail for Mailgun tests. Do not commit a private address if the repo is shared; use a test inbox.  
- Later: same CSV shape from LU IT, or LDAP. Sign-up code does not change.

### Email while data is fake

- Signup **code** is sent by the backend.  
- Local: Mailhog (`localhost:8025`).  
- Real inbox: only if that CSV row uses a real address and SMTP is Mailgun.  
- We no longer send a Keycloak password-setup **link** on sign-up.

### What we delete from the current flow

- File-number-first lookup  
- Confirm-details page as a separate step before create  
- Keycloak `executeActionsEmail` / UPDATE_PASSWORD link on student sign-up  
- Login with `@st.ul.edu.lb` as the username  

### What we do not build now

- LDAP / live LU portal  
- Jobs board, V2 plan, SSE  

---

## 4. V2 plan — WAITING FOR REVIEW

Not approved yet. Do not start building it.

Plan file: `.cursor/plans/luc_v2_semantic_matching_59c82301.plan.md`

**Short version:** one central list of topics (like "databases", "scholarships", "visa") where each topic gets an AI number-vector. Everything hangs off that list:

- Better search: mix word search with AI meaning search.
- Expert matching: send new questions to the alumni most likely to know the answer.
- Mentors: alumni directory + "find a mentor for X" + mentorship requests.
- Course pages for the LU Computer Science curriculum.

**Rules set for the plan:** it is for a master's thesis with a defense, so it needs measurable results. Time budget is 4 to 6 weeks. Scope is career + academic + AI only.

**Chosen NOT to build:** jobs board, masters-abroad application tracker, private messaging, real-time notifications (SSE).

**Note:** A simple mentorship request (student asks, alumnus accepts/declines) is already built. V2 still has topic-based “find a mentor for X”. Search title weighting used migration 016. Mentorship used 017. The old 013/014 XML patches were removed; mock students now live in `DataSeeder`.

---

## 5. Email — DECIDED (will be updated when auth road is approved)

- **Provider: Mailgun** for real inboxes later.  
- **Student sign-up code:** backend → Mailhog (mock) or Mailgun if the CSV email is real.  
- **Password on sign-up:** set immediately via Keycloak Admin API. No password-setup link.  
- **Forgot password:** still Keycloak’s own email.

### Code change — DONE

The backend no longer generates or emails passwords. It now:

1. Creates the Keycloak user with **no password**.
2. Asks Keycloak to send an `UPDATE_PASSWORD` action email (link valid 24 hours).
3. The student clicks the link and picks their own password.

Deleted as dead code for **passwords**: `SecurePasswordGenerator` and `KeycloakAdminClient.setPassword`.

`EmailService` is back, but only for the 6-digit **lookup** code. Password emails still come from Keycloak. Lookup codes go to **Mailhog** (`MAIL_HOST=mailhog`) because mock `@st.ul.edu.lb` addresses are not real mailboxes. Keycloak can stay on Mailgun for password links.

If the mail server is down, registration still succeeds and is only logged as an error — the student can recover the account with forgot-password. That avoids leaving a Keycloak user behind with no matching database row.

SMTP settings now live in `keycloak/realm-export.json` as `${KEYCLOAK_SMTP_*}` placeholders, filled in at container start by `keycloak/entrypoint.sh`. Defaults point at Mailhog, so local development needs no `.env` changes. Switching to Mailgun is only environment variables.

### Setup still to do

- Mailgun account, pick region (US or EU — this decides the SMTP host and cannot be changed easily later).
- A domain we control, with SPF and DKIM DNS records. We send **from** our domain **to** `@st.ul.edu.lb`. We do not need LU's permission for that.
- Get SMTP credentials from Mailgun (Sending → Domains → SMTP tab). These are not the website login and not the API key. The password is shown only once.
- Put them in `.env`: `KEYCLOAK_SMTP_HOST=smtp.mailgun.org`, `KEYCLOAK_SMTP_PORT=587`, `KEYCLOAK_SMTP_STARTTLS=true`, `KEYCLOAK_SMTP_AUTH=true`, `KEYCLOAK_SMTP_USER`, `KEYCLOAK_SMTP_PASSWORD`, and `KEYCLOAK_SMTP_FROM` on the verified domain. See `.env.example`.

### Gotchas

- Keycloak only imports `realm-export.json` when the realm does not exist yet. Editing that file on a running setup does nothing until the realm or database is recreated.
- The Keycloak "Test connection" button emails the admin account, so the admin user must have an email address set.
- Mailhog stays for local development forever. Real email is only needed when a real student must receive something — it does not block the thesis demo.

---

## 6. University data — FAKE DATA FOR NOW

We cannot reach LU systems until the professor approves access, so `student_directory` stays a hand-written table. That is the right seam: later we replace the rows (CSV, LDAP, or API) without rewriting sign-up.

### Auto-fill — DONE (on mock data)

Student types only a file number. We do **not** return the name immediately. We email a 6-digit code to the address already stored for that file. After the correct code, the form fills: name, email, year, faculty, major. Register then uses a one-time lookup token, not a typed email.

Lookup always returns the same message, even if the file number is unknown, so a stranger cannot scan 2024001, 2024002, … to copy the register. Lookups are also rate-limited.

### Ways to connect later, most likely first

1. **CSV / Excel export** from LU IT each semester, uploaded by an admin. Needs no work from their developers.
2. **Read-only database view** they create for us.
3. **REST API** — ideal, probably does not exist.
4. **LDAP / Active Directory** — the university's central account address book. Keycloak can connect to it directly, so students would log in with their existing university password. Then we create no passwords, send no passwords, and need no fake data. **Ask about this first.**

### Security warning for auto-fill — addressed

File numbers run in order, so a lookup that returns a name from a number alone would leak the register. We send a code first and rate-limit. Keep this when the real LU data arrives.

### Bonus for later

With real graduation records, alumni verification stops needing an admin. Today someone must open a LinkedIn URL and approve by hand. With the real register, proving control of a file number verifies the graduate instantly, and manual review becomes the fallback.

### Questions to ask LU IT

1. Do you have LDAP or Active Directory that students log in against?
2. Is there any API over the student information system?
3. Could we get a read-only view, or a periodic CSV export instead?
4. Is `st.ul.edu.lb` the only student mail domain, or do older students sit on something else?
5. Does the register keep graduation year and degree for past students, not only current ones?

---

## 7. Problems to fix

- [ ] **Test a clean start before the defense.** We chose to keep the existing dev database, so the Keycloak SMTP and registration settings were applied by hand in the admin console. That means `realm-export.json` and the running realm are only the same *in theory*. At least once before presenting, run `docker compose down -v && docker compose up -d --build` on a throwaway copy and confirm the whole system comes up correctly from the files alone.
- [ ] **Mailhog is abandoned.** Pinned at `mailhog/mailhog:v1.0.1` (2020, repo archived). `axllent/mailpit` is the maintained replacement, roughly a one-line swap in `docker-compose.yml`. Keycloak now depends on it, so it stays either way.
- [x] **Search relevance:** add `setweight` so title matches rank above body matches (migration 016).
- [x] ~~Wrong email domain and faculty in the fake student data~~ — mock students now seeded in `DataSeeder` with Faculty of Sciences CS and `@st.ul.edu.lb`. Old XML patches 013/014 were deleted.
- [x] ~~`registrationAllowed: true`~~ — now `false` in `keycloak/realm-export.json`. **Also needs to be switched off in the running realm**, see the warning below.

### Warning: realm-export.json does not update a realm that already exists

Keycloak imports that file only when the realm is missing. Our realm is already in Postgres, so the new SMTP settings and `registrationAllowed: false` will **not** apply by themselves. Either set them by hand in the admin console, or delete the `luc` realm and restart Keycloak so it re-imports (which erases all Keycloak users).
