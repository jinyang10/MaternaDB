# CareLink – Midwifery Service Database System

A Java-based healthcare database application designed to support maternal care workflows, including appointment management, clinical documentation, and diagnostic tracking.

---

## 🚀 Features

- Query midwives and scheduled appointments by date  
- View patient-specific appointment details  
- Review clinical notes and diagnostic test results  
- Add new observations (notes) to appointments  
- Prescribe diagnostic tests linked to patient records  
- Navigate appointment workflows through an interactive console interface  

---

## 🏗️ System Overview

The application connects to a **DB2 relational database** using JDBC and enables real-time interaction with structured healthcare data.

Key capabilities include:

- Multi-table relational queries across patients, practitioners, and appointments  
- Insert operations for notes and diagnostic test prescriptions  
- Indexed queries for efficient data retrieval  
- Data export for analytics (e.g., birth trends by month)  

The system models real-world healthcare entities such as:

- Patients (mothers, fathers, couples)  
- Pregnancies and babies  
- Midwives and healthcare institutions  
- Appointments, clinical notes, and diagnostic tests  

> The database consists of **15+ interconnected entities**, supporting complex healthcare workflows.

📄 Full schema and constraints available in:  
[`info.pdf`](./info.pdf)

---

## 🔄 Example Workflow

1. Enter practitioner ID  
2. Select appointment date  
3. View scheduled appointments  
4. Choose a specific appointment  
5. Perform actions:
   - Review notes  
   - Review tests  
   - Add a note  
   - Prescribe a test  

This workflow simulates real-world clinical interaction with patient records.

---

## 🛠️ Technologies

- **Java**
- **JDBC**
- **SQL (DB2)**
- **Relational Database Design**

---

## ▶️ How to Run

1. Configure DB2 credentials in `goBabbyApp.java`  
2. Compile and run:

```bash
javac goBabbyApp.java
java goBabbyApp
