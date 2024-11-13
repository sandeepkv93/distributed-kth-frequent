# 🎯 Distributed Kth Frequent Element Finder

A high-performance, distributed system for finding the kth most frequent element in a dataset. Built with Java 21 and designed for scalability and reliability.

## 🚀 Features

- **Distributed Processing**: Efficiently processes large datasets across multiple nodes
- **Memory-Aware**: Intelligent memory management with configurable thresholds per node
- **Fault Tolerant**: Handles node failures and timeouts gracefully
- **Highly Scalable**: Supports dynamic node configuration and load balancing
- **Thread-Safe**: Concurrent processing with proper synchronization
- **Comprehensive Testing**: Extensive test coverage including unit, integration, and stress tests

## 🛠️ Technical Stack

- Java 21 (with preview features)
- Maven for build management
- Lombok for reducing boilerplate
- SLF4J + Logback for logging
- JUnit 5 for testing
- Google Java Format for consistent code style
- Apache Commons & Google Guava utilities

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.8+
- At least 1GB of RAM (configurable based on dataset size)

## 🏗️ Building the Project

```bash
mvn clean install
```

This will:

1. Compile the source code
2. Run the tests
3. Create an executable JAR with dependencies
4. Format the code using Google Java Format

## 🎮 Usage

### Basic Example

```java
// Create a coordinator with 3 nodes and 1MB memory per node
Coordinator coordinator = new Coordinator(3, 1024 * 1024);

// Find the 3rd most frequent element
List<Integer> data = Arrays.asList(9, 9, 6, 9, 8, 6, 8, 6, 4);
int result = coordinator.findKthFrequent(data, 3);

```

### Advanced Configuration

```java
// Configure for large datasets with more nodes
int numNodes = 8;
long memoryPerNode = 2 * 1024 * 1024;// 2MB per node
Coordinator coordinator = new Coordinator(numNodes, memoryPerNode);

// Process millions of records
List<Integer> largeDataset = generateLargeDataset(1_000_000);
int result = coordinator.findKthFrequent(largeDataset, 5);

```

## 🏛️ Architecture

The system consists of three main components:

### 1. Coordinator (Coordinator.java)

- Manages distributed processing workflow
- Handles data partitioning and distribution
- Aggregates results from nodes
- Implements fault tolerance mechanisms

### 2. Processing Nodes (ProcessingNode.java)

- Processes data partitions independently
- Maintains memory usage within thresholds
- Reports processing status and results
- Handles local error recovery

### 3. Data Models

- `DataPartition.java`: Encapsulates data chunks for processing
- `FrequencyPair.java`: Represents element-frequency pairs
- `ProcessingResult.java`: Contains node processing results

```mermaid
sequenceDiagram
    participant C as Coordinator
    participant N1 as Node 1
    participant N2 as Node 2
    participant N3 as Node 3

    Note over C,N3: Phase 1: Data Distribution
    C->>N1: Chunk 1 of data
    C->>N2: Chunk 2 of data
    C->>N3: Chunk 3 of data

    Note over C,N3: Phase 2: Local Processing
    activate N1
    N1->>N1: Count frequencies
    activate N2
    N2->>N2: Count frequencies
    activate N3
    N3->>N3: Count frequencies

    Note over C,N3: Phase 3: Send Local Results
    N1-->>C: Local frequency dict 1
    deactivate N1
    N2-->>C: Local frequency dict 2
    deactivate N2
    N3-->>C: Local frequency dict 3
    deactivate N3

    Note over C: Phase 4: Global Processing
    activate C
    C->>C: Merge frequency dictionaries
    C->>C: Create max heap
    C->>C: Extract Kth frequent element
    deactivate C

    Note over C: Phase 5: Memory Management
    activate C
    C->>C: Check memory usage
    alt Memory fits
        C->>C: Direct processing
    else Memory exceeded
        C->>C: Use Count-Min Sketch
        C->>C: Process in batches
    end
    deactivate C
```

```mermaid
flowchart TD
    A[Input Large Dataset] --> B[Distribute Data]
    
    subgraph Local_Processing["Local Processing (Map Phase)"]
        B --> C1[Node 1]
        B --> C2[Node 2]
        B --> C3[Node 3]
        
        C1 --> D1[Count Local Frequencies]
        C2 --> D2[Count Local Frequencies]
        C3 --> D3[Count Local Frequencies]
        
        D1 --> E1[Local Dict 1]
        D2 --> E2[Local Dict 2]
        D3 --> E3[Local Dict 3]
    end
    
    subgraph Global_Processing["Global Processing (Reduce Phase)"]
        E1 --> F[Merge Dictionaries]
        E2 --> F
        E3 --> F
        F --> G[Global Frequency Dict]
    end
    
    subgraph Memory_Management["Memory Management"]
        G --> H{Memory Check}
        H -->|Fits| I[Direct Processing]
        H -->|Exceeded| J[Batch Processing]
        J --> K[Count-Min Sketch]
    end
    
    subgraph Final_Processing["Final Processing"]
        I --> L[Create Max Heap]
        K --> L
        L --> M[Extract K Elements]
        M --> N[Return Kth Element]
    end
    
    classDef default fill:#f9f9f9,stroke:#333,stroke-width:2px
    classDef phase fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    
    class Local_Processing,Global_Processing,Memory_Management,Final_Processing phase
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Test

# Run integration tests
mvn verify -P integration-tests

```

Test categories:

- 🎯 Unit Tests: Individual component testing
- 🔄 Integration Tests: End-to-end workflow testing
- 💪 Stress Tests: Performance and stability testing
- 🐛 Edge Cases: Boundary condition testing
- 🔀 Concurrent Tests: Multi-threading scenarios

## 📊 Performance Characteristics

The system is optimized for:

- 📈 Large datasets (millions of records)
- 🔄 Multiple concurrent requests
- 💾 Memory-constrained environments
- 📊 Various data distributions:
    - Normal distribution
    - Skewed data
    - Sparse datasets
    - Power law distribution

## 🚀 Getting Started

1. Clone the repository:

```bash
git clone https://github.com/yourusername/distributed-kth-frequent.git

```

2. Navigate to project directory:

```bash
cd distributed-kth-frequent

```

3. Build the project:

```bash
mvn clean install

```

4. Run the example:

```bash
java -jar target/kth-frequent-1.0-SNAPSHOT-jar-with-dependencies.jar

```
