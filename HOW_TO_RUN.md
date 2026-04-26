# StudySync Web App — How to Run

---

## Folder Structure

```
studysync/
├── src/
│   ├── main/
│   │   ├── java/com/studysync/
│   │   │   ├── StudySyncApplication.java   ← Spring Boot entry point
│   │   │   ├── model/
│   │   │   │   ├── Subject.java
│   │   │   │   ├── Topic.java
│   │   │   │   └── TopicStatus.java
│   │   │   ├── service/
│   │   │   │   ├── SubjectStore.java
│   │   │   │   ├── ProgressTracker.java
│   │   │   │   └── PlanValidator.java
│   │   │   └── controller/
│   │   │       └── StudySyncController.java  ← REST API
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html               ← Web UI
│   │           ├── css/style.css
│   │           └── js/app.js
│   └── test/java/com/studysync/
│       └── StudySyncTest.java
├── jenkins/Jenkinsfile
├── sonar/sonar-project.properties
└── pom.xml
```

---

## Step 1 — Install Tools

| Tool       | Version  | Download                                   |
|------------|----------|--------------------------------------------|
| JDK        | 11       | https://adoptium.net                       |
| Maven      | 3.8+     | https://maven.apache.org/download.cgi      |
| Git        | Any      | https://git-scm.com                        |
| Jenkins    | LTS      | https://www.jenkins.io/download            |
| SonarQube  | 9.x      | https://www.sonarsource.com/sonarqube      |
| Artifactory| OSS      | https://jfrog.com/artifactory              |

---

## Step 2 — Run the Web App Locally

```bash
# 1. Go into the project folder
cd studysync

# 2. Run the Spring Boot app
mvn spring-boot:run

# 3. Open your browser
# Go to → http://localhost:8080
```

That's it. The full StudySync UI will open in your browser.

---

## Step 3 — Run Tests

```bash
# Run all JUnit tests
mvn test

# Run tests + generate JaCoCo coverage report
mvn test jacoco:report

# View coverage report in browser:
# Open → target/site/jacoco/index.html
```

---

## Step 4 — Build the Jar

```bash
mvn package -DskipTests
# Output: target/studysync-1.0.0.jar

# Run the jar directly:
java -jar target/studysync-1.0.0.jar
# Open → http://localhost:8080
```

---

## Step 5 — Push to Git

```bash
git init
git add .
git commit -m "Initial commit - StudySync web app"
git remote add origin https://github.com/YOUR_USERNAME/studysync.git
git push -u origin main
```

---

## Step 6 — Set Up SonarQube

```bash
# Start SonarQube
# Windows: bin\windows-x86-64\StartSonar.bat
# Linux:   bin/linux-x86-64/sonar.sh start

# Open → http://localhost:9000
# Login: admin / admin

# Run scan:
mvn sonar:sonar \
  -Dsonar.projectKey=studysync \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=YOUR_TOKEN
```

---

## Step 7 — Set Up Jenkins

1. Open Jenkins → http://localhost:8080
2. Install plugins: Git, Maven Integration, SonarQube Scanner, JaCoCo, Pipeline
3. Manage Jenkins → Configure System → Add SonarQube server (http://localhost:9000)
4. New Item → Pipeline → Pipeline from SCM → your Git repo URL
5. Script path: `jenkins/Jenkinsfile`
6. Click **Build Now**

---

## Pipeline Flow

```
Git Checkout
     ↓
SonarQube Scan      ← code quality, bugs, smells
     ↓
Quality Gate        ← BLOCKS if quality fails
     ↓
JUnit Tests         ← 18 test cases run
     ↓
JaCoCo Coverage     ← BLOCKS if coverage < 75%
     ↓
Maven Build         ← creates studysync-1.0.0.jar
     ↓
Artifactory Upload  ← stores versioned artifact
     ↓
Deploy              ← app runs at http://localhost:8080
```

---

## REST API Endpoints (for reference)

| Method | URL                                              | What it does          |
|--------|--------------------------------------------------|-----------------------|
| GET    | /api/subjects                                    | Get all subjects      |
| GET    | /api/stats                                       | Get dashboard stats   |
| POST   | /api/subjects                                    | Add a subject         |
| DELETE | /api/subjects/{id}                               | Remove a subject      |
| POST   | /api/subjects/{id}/topics                        | Add a topic           |
| DELETE | /api/subjects/{id}/topics/{topicId}              | Remove a topic        |
| PATCH  | /api/subjects/{id}/topics/{topicId}/cycle        | Cycle topic status    |

---

## What to Show in Evaluation

1. Open browser → http://localhost:8080 (live web app)
2. Add subjects with exam dates and topics live
3. Jenkins → show all 7 stages green
4. SonarQube → show 0 bugs, 0 vulnerabilities
5. JaCoCo report → show 75%+ coverage
6. Artifactory → show uploaded jar
