# 🚀 2D Space Shooting Game

![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![OOP](https://img.shields.io/badge/Architecture-Object_Oriented_Programming-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

## 📖 About The Project / 關於專案
A classic 2D vertical scrolling shooting game developed entirely in **Java**. This project demonstrates the practical application of **Object-Oriented Programming (OOP)** concepts, including inheritance, polymorphism, and encapsulation, to manage complex game states and entity interactions.

這是一部完全使用 **Java** 開發的經典 2D 縱向捲軸射擊遊戲。本專案重點展示了**物件導向程式設計 (OOP)** 的實務應用，包括繼承、多型與封裝，藉此有效管理複雜的遊戲狀態與實體間的互動邏輯。

---

## ✨ Key Features / 核心功能
* **Player Mechanics:** Smooth multi-directional movement and shooting control.
    * **玩家機制：** 流暢的多方向移動與射擊控制。
* **Enemy AI:** Diverse enemy types with different movement patterns and attack logic.
    * **敵人 AI：** 具備多樣化移動路徑與攻擊邏輯的敵機類型。
* **Collision Detection:** Precise hitbox calculation between bullets, player, and enemies.
    * **碰撞偵測：** 子彈、玩家與敵人之間精確的碰撞體 (Hitbox) 計算。
* **Resource Management:** Optimized dynamic loading for sprites (`image/`) and audio (`resources/`) to ensure stable frame rates (FPS).
    * **資源管理：** 優化圖片與音效的動態載入，確保遊戲畫面更新率穩定。

---

## 🛠️ Tech Stack & Concepts / 技術棧與開發概念
* **Language:** Java (Swing/AWT for rendering)
* **Design Patterns / 設計模式：**
    * **State Pattern:** Manages transitions between different game stages (Menu, Playing, Game Over).
        * **狀態模式：** 管理遊戲不同階段（選單、戰鬥中、遊戲結束）的狀態切換。
    * **Singleton Pattern:** Ensures efficient resource management for images and audio.
        * **單例模式：** 確保資源管理器在記憶體中僅存在單一實例。
* **Version Control:** Git & GitHub (Collaborative development with a 3-person team).
    * **版本控制：** 使用 Git 與 GitHub 進行團隊協作開發。

---

## 📂 Folder Structure / 檔案結構
* `src/`: Java source code (Game logic, Entities, Controllers).
* `image/`: Visual assets and sprite sheets.
* `resources/`: Audio files and game configuration data.