<div align="center">

# 🖥️ ECPC: Efficient Custom Personal Computer Builder

**Batangas State University — The National Engineering University**  
*College of Informatics and Computing Sciences | Computer Science Department*  
*OOP 101: Object-Oriented Programming (1st Semester, AY 2026-2027)*

[![Java](https://img.shields.io/badge/Language-Java-orange.svg)](https://www.oracle.com/java/)
[![Status](https://img.shields.io/badge/Status-In%20Development-blue.svg)]()
[![Section](https://img.shields.io/badge/Section-CS%202101-green.svg)]()

</div>

---

## 📖 About the Project
Building a custom personal computer is a technical task that can be overwhelming for beginners. Choosing incompatible parts or mismanaging your budget leads to wasted time, money, and hardware performance bottlenecks. 

**ECPC (Efficient Custom Personal Computer Builder)** is a Java-based gui application designed to help students, first-time PC builders, and hobbyists plan, validate, and optimize custom PC builds based on budget, intended workload (schoolwork, gaming, or video editing), and brand preferences.

---

## 🎯 Problem Being Addressed
* **Hardware Incompatibility:** Manually verifying sockets, RAM types, power draws, and case clearances is error-prone.
* **Budget Misallocation:** Users often overspend on unbalanced components (e.g., pairing an expensive GPU with a weak CPU).
* **Technical Complexity:** Beginners lack intuitive guidance when navigating complex hardware specifications.

---

## 🚀 Project Objectives

### General Objective
To develop a Java-based application that guides users in designing a compatible PC build within their budget by validating component compatibility and power requirements, rating the CPU-GPU balance, and recommending parts tailored to the user's workload.

### Specific Objectives
1. **Parts Catalog:** Load CPUs, motherboards, GPUs, RAM, storage, power supplies, and cases from a local data file with specifications, performance scores, and prices.
2. **Compatibility Validation:** Check CPU-motherboard sockets, RAM types/capacities, case form factors vs. GPU lengths, and PSU wattage safety margins.
3. **Auto-Build:** Automatically recommend a complete, compatible build within a specified minimum and maximum budget range.
4. **Bottleneck Detector:** Rate CPU-GPU balance as *Balanced*, *Moderate*, or *Severe* for the chosen workload.
5. **Custom Builds:** Allow users to create, edit, save, and load custom PC builds.
6. **Application of Object-Oriented Programming Principles:** Demonstrate core Object-Oriented Programming principles to ensure extensibility for new component types.

---

## 👥 Target Users

| Target User | How They Will Use the System |
| :--- | :--- |
| **Students** | Enter a budget range and school/light-gaming workload to receive affordable, compatible part recommendations. |
| **First-Time PC Buyers** | Input preferences to generate optimized builds while receiving warnings against incompatibilities or performance mismatches. |
| **PC Hobbyists** | Input custom part lists through the Custom Build feature to check compatibility, power draw, and bottlenecks before saving. |

---

## ✨ Main Features

* 🔍 **Compatibility Checker:** Verifies socket types, dimensions, form factors, and power sufficiency.
* ⚖️ **Bottleneck Detector:** Compares CPU and GPU performance relative to the chosen workload to flag potential performance bottlenecks.
* ⚙️ **Auto Build and Budget Optimizer:** Generates optimal builds matching user budgets and brand preferences.
* 🛠️ **Custom Build:** Lets users add, replace, or remove specific parts for a build they have in mind, with the running total cost and compatibility status shown as they edit.
* 💾 **Save & Load Builds:** Persist and retrieve your custom configurations from local files.

---

## 🛠️ Technologies Used
* **Programming Language:** Java
* **GUI Framework:** Java Swing
* **Data Format:** JSON (for storing PC component information)
* **Development Environment:** Visual Studio Code (VS Code)
* **Version Control:** Git and GitHub
* **Java Development Kit (JDK):** JDK 21
* **Compilation Tool:** Java Compiler (javac)
* **Application Packaging Tool:** Java Archive Tool (jar)
* **Application Distribution Format:** Executable JAR (.jar)
* **File Handling:** Java I/O and NIO (for saving and loading custom PC builds)

---

## 🧱 OOP Concepts Demonstrated

## 🧩 9. OOP Concepts Demonstrated

* **Classes and Objects:** Defines blueprints for hardware and system logic (such as `Component`, `CPU`, `Motherboard`, `GPU`, `RAM`, `Storage`, `PowerSupply`, `PCCase`, `PCBuild`, and `PartCatalog`), with objects representing individual catalog parts, the user's current build, and generated reports.
* **Methods:** Encapsulates core operations including calculating total costs and estimated wattage (`PCBuild`), running compatibility checks (`CompatibilityChecker`), computing CPU-GPU balance (`BottleneckAnalyzer`), and generating recommended builds (`BuildRecommender`).
* **Encapsulation:** Keeps attributes like price, socket type, wattage, dimensions, and performance scores private and accessed via public getters, while catalog parts have no setters to prevent modification after loading and build updates pass through validating methods like `addComponent()`.
* **Inheritance:** Uses the abstract class `Component` to hold shared attributes (`ID`, `name`, `brand`, `price`), which `CPU`, `Motherboard`, `GPU`, `RAM`, `Storage`, `PowerSupply`, and `PCCase` extend to add their own specifications and avoid repeated code.
* **Polymorphism:** Handles all parts uniformly as `Component` objects (such as storing them in a single list and displaying them in a single loop) while each subclass overrides methods like `getSpecs()` and `checkCompatibility()` with specialized behavior.
* **Abstraction:** Relies on the abstract class `Component` to declare mandatory methods for every part, hiding complex calculations like wattage totals and bottleneck formulas behind simple method calls for the menu code.
* **Exception Handling:** Employs custom exceptions (such as `IncompatibleComponentException`, `BudgetExceededException`, and `InvalidInputException`) alongside try-catch blocks to handle invalid inputs, incompatible parts, unfeasible budgets, and missing or corrupted files without crashing the program.
* **Arrays/Data Handling:** Utilizes `ArrayList` collections to store the parts catalog, the components in the active build, and the collection of saved builds.

---

## 👷 Team Members & Roles

| Team Member | Role | Assigned Tasks |
| :--- | :--- | :--- |
| **Palma, Tristan P.** | Project Leader | Coordinates tasks, monitors deadlines, communicates with the instructor, and facilitates team meetings. |
| **Carandang, Sean Christ Trez' Davidoff A.** | Lead Programmer | Coordinates the implementation of Java classes, methods, and OOP features. |
| **Palor, Christian Gene C.** | System / Design Lead | Develops system requirements, class designs, UML diagrams, and overall application structure. |

---

## 💻 Instructions for Running the Program

1. 

---

## 📸 Sample Screenshots


---

## ⚠️ Known Limitations
* **No Live Pricing:** Prices and specifications are derived from a static local dataset.
* **Estimated Benchmarks:** Performance scores and bottleneck ratings rely on fixed reference scores rather than real-time hardware diagnostics.
* **Standard Desktop Parts Only:** Specialized server hardware, laptops, pre-builts, custom liquid cooling loops, and individual CPU coolers/case fans are excluded.
* **Local Storage Only:** No user accounts or cloud storage features; all saved builds reside in local files.

---

## 🔮 Future Improvements
Although ECPC already provides essential PC building and recommendation functionalities, several improvements can be considered for future versions:

* **Expanded Component Database:** Include more PC components, brands, models, and updated hardware specifications to provide users with a wider range of choices.
* **More Accurate Bottleneck Analysis:** Improve the analysis algorithm by incorporating more realistic performance data, workload-specific calculations, and benchmark information.
* **Advanced Compatibility Checking:** Expand compatibility validation to cover additional hardware specifications and requirements, ensuring more accurate component recommendations.
* **Improved Build Recommendations:** Enhance the recommendation algorithm to consider more factors, such as component performance, price-to-performance ratio, upgrade potential, and user preferences.
* **Price Updates and Comparison:** Introduce updated component pricing and price comparisons from different retailers to help users make more cost-effective decisions.
* **Build Exporting and Sharing:** Allow users to export their PC configurations into formats such as PDF or share their builds with other users.
* **Enhanced User Interface:** Further improve the graphical interface with additional customization options, better navigation, and a more user-friendly experience.
* **User Accounts and Build History:** Introduce user accounts that allow users to save multiple PC configurations and access their previous builds.

---

## 🗂️ Project Contributors & Repository
* **Team Name:** An Family (Section: CS 2101)
* **GitHub Repository:** [ECPC-Efficient-Custom-Personal-Computer-Builder](https://github.com/AN-FAMILY-OOP101/ECPC-Efficient-Custom-Personal-Computer-Builder.git)
