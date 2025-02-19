# Google Docs Clone App

<div align="center">
  <img src="https://github.com/user-attachments/assets/9f40331c-3453-4547-bdf9-84c60baab71a" alt="Google Docs Logo" width="150"/>
</div>

**App APK Link:** [Download Here]([https://drive.google.com/file/d/1nK0rYodVF5itgi8OC5zF6EP7U82FoDfz/view?usp=sharing])  

Check out the app demo on YouTube: [**Google Docs App Demo**](https://youtu.be/DuO1DCR1MYc?si=iWqGoziqMe7T_PCA)

---

## Overview ✨

This project is a **Google Docs Clone** app featuring a sleek **white-themed background**. It allows users to effortlessly create, edit, view, and manage documents through a clean and intuitive interface. The app offers a variety of features, including **Google authentication**, **offline document saving**, **rich text editing**, **document sharing**, and **PDF export**. 📄

The app leverages **Firebase Firestore** for cloud storage and **Room Database** for offline saving, ensuring seamless document management both online and offline.

---

## Screenshots 📸

<table align="center">
  <tr>
    <td>
      <img src="https://github.com/user-attachments/assets/edd19175-7717-423a-9f2e-8704f95ecdc9" alt="Screenshot 10" width="200"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/7388deb2-549d-478b-8cb1-9b8956106bbb" alt="Screenshot 9" width="200"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/15b8499a-cd0c-425e-ad05-d8fb85e1c23d" alt="Screenshot 8" width="200"/>
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/user-attachments/assets/8d1423d3-761b-4ca8-beef-dea4b0febe93" alt="Screenshot 7" width="200"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/0a371564-9d2c-4c33-846e-7babb606d1e4" alt="Screenshot 6" width="200"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/fe48e092-edc6-4b01-a3b2-cff143775ed5" alt="Screenshot 5" width="200"/>
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/user-attachments/assets/1b9bb061-a4dd-4bbd-9f82-0c84d8fb8d81" alt="Screenshot 4" width="200"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/3c74c244-63f4-45db-a510-900f917bc9d7" alt="Screenshot 3" width="200"/>
    </td>
    <td>
      <img src="https://github.com/user-attachments/assets/67b44a7b-bbdc-4b8d-b957-45c27255ebe4" alt="Screenshot 2" width="200"/>
    </td>
  </tr>
  <tr>
    <td>
      <img src="https://github.com/user-attachments/assets/570e60a3-a335-4683-83b9-80fe0ca02f98" alt="Screenshot 1" width="200"/>
    </td>
  </tr>
</table>

---

## Screen Recordings 🎥

<table align="center">
  <tr>
    <td>
      <video src="https://github.com/user-attachments/assets/79373bc6-a46b-4a75-9420-be6fd2f46714" controls width="300"></video>
    </td>
    <td>
      <video src="https://github.com/user-attachments/assets/65f5bc21-fa1b-42b4-b6c7-c760c4f77246" controls width="300"></video>
    </td>
  </tr>
  <tr>
    <td>
      <video src="https://github.com/user-attachments/assets/06f3eaaf-f37f-4e17-996b-ed4fef44fd1d" controls width="300"></video>
    </td>
    <td>
      <video src="https://github.com/user-attachments/assets/4e5b86e4-4565-4aa0-b0f9-d1d60af0879e" controls width="300"></video>
    </td>
  </tr>
  <tr>
    <td>
      <video src="https://github.com/user-attachments/assets/5e907950-fab9-419e-8ed3-d48ebf4ceb5c" controls width="300"></video>
    </td>
    <td>
      <video src="https://github.com/user-attachments/assets/e0003b8a-adde-4ff5-8676-ef578f8dc289" controls width="300"></video>
    </td>
  </tr>
  <tr>
    <td>
      <video src="https://github.com/user-attachments/assets/03eb2342-5299-47cb-9667-e8157cb09141" controls width="300"></video>
    </td>
  <td>
      <video src="https://github.com/user-attachments/assets/your-video-file" controls width="300"></video>
    </td>
  </tr>
</table>

---

## Features 🚀

### 1. **Login Page 🔐**
- Supports only **Google authentication**.
- Users can sign in using their Google account with ease.

### 2. **Home Screen 🏠**
- Displays a list of documents with **name** and **last updated time**.
- Key actions on each document:
  - **Save offline**: Saves documents in **Room Database** for offline access.
  - **Delete**: Removes documents from **Firestore**.
- A **Floating Action Button (FAB)** to create new documents or open existing ones.

### 3. **Document Creation & Editing 🖋**
- Create new documents by providing a **title** and **content**.
- Rich text editing features including **bold**, **italic**, **underline**, and **font size** adjustment.
- Save documents to **Firebase Firestore** after editing.

### 4. **Offline Support 🌐**
- Save documents offline with **Room Database**.
- View and edit documents without an internet connection.

### 5. **Document Viewing 👀**
- Clicking on a document opens a **view-only mode**, displaying the content in a WebView-like format.
- Ensures accurate representation of formatted text.

### 6. **Document Sharing 🤝**
- Share document links with **view or edit access** for other users.
- Export and share documents as **PDF files**.

### 7. **PDF Download 📥**
- Download documents as **PDF files** with ease.
- A notification is displayed once the PDF is successfully saved to the device.

---

## Technologies Used 🛠️

- **Kotlin**: The main programming language for development.
- **Jetpack Compose**: To build the app’s UI.
- **Firebase Firestore**: For cloud storage.
- **Room Database**: For offline document storage.
- **Google Authentication**: For login and user management.
- **WebView**: To display documents in **view-only mode**.
- **PDF Export**: To save documents as PDFs.

---

## How to Run the App ⚙️

1. **Clone** this repository.
   ```bash
   git clone https://github.com/yourusername/google-docs-clone.git
   ```
2. Set up **Firebase** for your project.
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project and add your Android app.
   - Download the `google-services.json` file and place it in the `app` directory.
3. Enable **Google Authentication** in Firebase.
4. Sync the project in **Android Studio**.
5. Build and run the app on your device.

---

## Future Improvements 🎯

- Add more text formatting options in the editor.
- Enhance the **PDF export** feature with advanced formatting.
- Implement **real-time collaboration** on documents.

---

## Contributing 🤝

We welcome contributions to the project! If you have suggestions or improvements, please follow these steps:

1. **Fork the Repository**
2. **Create a New Branch**
3. **Make Your Changes**
4. **Submit a Pull Request**

---

## License 📜

This project is licensed under the [MIT License](LICENSE).

---

## Contact 📧

For any inquiries or issues, please contact us at [mohitrdamke@gmail.com](mailto:mohitrdamke@gmail.com).

---

Thank you for checking out the **Google Docs Clone** app! We hope you enjoy using it as much as we enjoyed building it. 🚀
