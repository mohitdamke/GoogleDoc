# Google Docs Clone App

## Overview

This project is a **Google Docs Clone** app with a clean **white theme background**. It allows users to create, edit, view, and manage documents in a simple and intuitive interface. The app provides various features such as Google authentication, offline saving, rich text editing, document sharing, and PDF export. 

The app uses **Firebase Firestore** for cloud storage of documents and **Room Database** for offline document saving.

## Screenshots

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/403f4666-c5a1-46f8-ac65-986a0c4619a0" alt="Screenshot 1" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/7fd3d1b3-727c-4e77-81ac-957a98980378" alt="Screenshot 2" width="300"/></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/7fe24b67-0b66-4c4e-96dc-255048bf862f" alt="Screenshot 3" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/9405aa08-a8a8-408a-b9c2-4c2562caece2" alt="Screenshot 4" width="300"/></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/e6dc651f-dc91-4d49-9543-97022ca10694" alt="Screenshot 5" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/f174beac-05ab-4e80-a64e-3728d1640d01" alt="Screenshot 6" width="300"/></td>
  </tr>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/5e4e335d-52c1-4818-a3b9-22a80cb4cb66" alt="Screenshot 7" width="300"/></td>
    <td><img src="https://github.com/user-attachments/assets/1e30ce6a-9a75-4b3c-9ef4-8da61ab5bed0" alt="Screenshot 8" width="300"/></td>
  </tr>
</table>

## Screen Recording

<table>
  <tr>
    <td><video src="https://github.com/user-attachments/assets/b603eca6-27cb-4138-8966-ad858c36184e" controls width="300"></video></td>
    <td><video src="https://github.com/user-attachments/assets/d4ce4791-2953-46a8-990c-6650aa483ec0" controls width="300"></video></td>
  </tr>
  <tr>
    <td><video src="https://github.com/user-attachments/assets/78a257b2-df4e-45ec-8ca7-b3c7b7a0d835" controls width="300"></video></td>
    <td><video src="https://github.com/user-attachments/assets/f60e9b41-a937-4950-92ee-f802c123790f" controls width="300"></video></td>
  </tr>
</table>

## Features

### 1. **Login Page**
- Only Google authentication is supported.
- Users can sign in using their Google account.

### 2. **Home Screen**
- Displays a list of documents with their **name** and **last updated time**.
- Two key options on each document:
  - **Save offline**: Saves the document in the Room database for offline access.
  - **Delete**: Deletes the document from Firestore.
- **Floating Action Button (FAB)** to create a new document or open an existing one.

### 3. **Document Creation & Editing**
- Users can:
  - **Create a new document** by providing a title and content.
  - Edit the text with formatting options like **bold**, **italic**, **underline**, and change the **font size**.
  - Save the document to **Firebase Firestore** after editing.
  
### 4. **Offline Support**
- Documents can be saved offline using **Room Database**.
- Users can view and edit documents without an internet connection.

### 5. **Document Viewing**
- When clicking on a document, users enter a **view-only mode** where they can see the document's content in a WebView-like format.
- The view-only mode ensures users can see the formatted text as expected.

### 6. **Document Sharing**
- Users can share document links and **grant view or edit access** to other users.
- Share document content as **PDF** files.

### 7. **PDF Download**
- Documents can be downloaded as **PDF files**.
- A notification is displayed once the PDF is downloaded and saved to the device's storage.

## Technologies Used
- **Kotlin**: Main programming language.
- **Jetpack Compose**: For building the UI.
- **Firebase Firestore**: For storing documents in the cloud.
- **Room Database**: For offline storage of documents.
- **Google Authentication**: For user login and authentication.
- **WebView**: For displaying documents in view-only mode.
- **PDF Export**: To convert documents into PDF files.

## How to Run the App

1. Clone this repository.
2. Set up **Firebase** for your project.
3. Enable **Google Authentication** in Firebase.
4. Sync the project with **Android Studio**.
5. Build and run the app on your device.

## Future Improvements
- Add more customization options in the text editor.
- Improve PDF export features with advanced formatting.
- Implement collaborative editing in real-time.

---

Feel free to contribute to this project by submitting pull requests or reporting any issues!
