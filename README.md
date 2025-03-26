# NFramework

A lightweight, annotation-based networking framework that enables easy client-server communication using a custom request-response model.

## 🚀 What I Learned from Building NFramework

Developing **NFramework** has been an insightful journey. It deepened my understanding of **network programming, request handling, and method invocation in Java**. While WebSockets and REST APIs are widely used, building a **custom networking framework** allowed me to explore **serialization, annotation-based APIs, and direct method execution on the server**.

This project reinforced key lessons about **protocol design, performance considerations, and structured communication** between a client and a server. It also helped me appreciate the **strengths of WebSockets and REST APIs** while giving me the freedom to **experiment with a custom approach**.

## 🔥 Why NFramework is Interesting

✔️ **Annotation-based API** for defining endpoints.  
✔️ **Lightweight** with no external dependencies.  
✔️ Supports **serializable object transmission**.  
✔️ Custom **request-response model** for optimized performance.  
✔️ Easy-to-use client with simple `execute()` calls.  
✔️ **Automatic method invocation** on the server.  

While **NFramework** isn’t meant to replace existing solutions, it serves as a **learning tool** and a **practical alternative** for handling lightweight request-response models efficiently.

## 🛠 Installation & Setup

### **1️⃣ Download the JAR Files**

Get the latest release of **NFramework Server & Client JARs** from the [Releases Page](https://github.com/Mohammeddaniyal/NetworkFramework/releases/tag/v1.0) and include them in your project.

### **2️⃣ Setting Up the Server**

#### **📥 Required Imports**
```java
import com.thinking.machines.nframework.server.*;
import com.thinking.machines.nframework.server.annotations.*;
```

#### **🛠 Implementation**
```java
@Path("/banking")
public class BankService {
    @Path("/branchName")
    public String getBranch(String city) {
        return city.equals("Mumbai") ? "Colaba" : "Unknown Branch";
    }
}
```

#### **🔧 Starting the Server**
```java
import com.thinking.machines.nframework.server.*;

public class BankServer {
    public static void main(String[] args) {
        NFrameworkServer server = new NFrameworkServer();
        server.registerClass(BankService.class);
        server.start();
    }
}
```

### **3️⃣ Creating a Client**

#### **📥 Required Imports**
```java
import com.thinking.machines.nframework.client.*;
```

#### **🛠 Implementation**
```java
public class BankClient {
    public static void main(String[] args) {
        try {
            NFrameworkClient client = new NFrameworkClient();
            String branch = (String) client.execute("/banking/branchName", "Mumbai");
            System.out.println("Branch: " + branch);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### **4️⃣ Running the Framework**

```sh
# Run the Server
java -cp nframework-server.jar BankServer

# Run the Client
java -cp nframework-client.jar BankClient
```

### 📌 **Expected Output**
```sh
Branch: Colaba
```

## ⚡ Future Enhancements

- Configurable **port selection** (currently hardcoded to `5500`).
- Improved **exception handling** for better debugging.
- Performance optimizations and **async request handling**.

## 📜 License

This project is licensed under the **MIT License**. Feel free to modify and contribute!

---

> **NFramework** – A Simple Yet Insightful Networking Experience 🚀