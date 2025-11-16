# OrionDB Chess Database Engine

**OrionDB** is a high-performance, open-source chess database engine written in Java. It is designed for developers who need to build applications with rich, fast search functionality over large collections of chess games.

Unlike traditional chess applications, OrionDB is a library, not a GUI. It provides a powerful, developer-centric API to create, manage, and query chess databases programmatically.

## Key Features

- **High-Performance PGN Parsing**: A fast, error-tolerant parser that can import millions of games from standard PGN files.
- **Optimized Binary Format**: A custom `.oriondb` binary format for compact storage and extremely fast read operations.
- **Rich Metadata Indexing**: Multi-layered indexes for all standard PGN tags, including player, event, ECO, date, Elo, and result.
- **Fluent Query API**: A clean, chainable, and type-safe API for building complex search queries.
- **Minimal Dependencies**: Built with the Java 11+ standard library, with no external runtime dependencies.
- **Open Source**: Licensed under the MIT License.

## Getting Started

### Prerequisites

- Java 11 or higher
- Apache Maven (for building)

### Building from Source

1.  **Clone the repository:**

    ```bash
    git clone https://github.com/your-username/oriondb.git
    cd oriondb
    ```

2.  **Build with Maven:**

    ```bash
    mvn clean install
    ```

    This will compile the project and create a JAR file in the `target/` directory.

## Usage

### 1. Creating a Database

First, you need to create a database from a PGN file. OrionDB will parse the PGN, write the games to its optimized binary format, and create an index file.

```java
import com.oriondb.core.OrionDatabase;
import com.oriondb.util.ProgressCallback;
import java.io.File;

public class CreateDatabase {
    public static void main(String[] args) throws Exception {
        File pgnFile = new File("path/to/your/games.pgn");
        File dbFile = new File("path/to/your/games.oriondb");

        System.out.println("Creating database...");

        OrionDatabase.ImportStats stats = OrionDatabase.createFromPgn(
            pgnFile, 
            dbFile, 
            ProgressCallback.CONSOLE // Reports progress to the console
        );

        System.out.println("\nDatabase created successfully!");
        System.out.println(stats);
    }
}
```

### 2. Loading and Querying a Database

Once the database is created, you can load it and perform queries using the fluent `SearchBuilder` API.

```java
import com.oriondb.core.OrionDatabase;
import com.oriondb.model.Game;
import java.io.File;
import java.util.List;

public class QueryDatabase {
    public static void main(String[] args) throws Exception {
        File dbFile = new File("path/to/your/games.oriondb");

        try (OrionDatabase db = OrionDatabase.load(dbFile)) {
            System.out.println("Database loaded with " + db.getGameCount() + " games.");

            // Example 1: Find all games by Magnus Carlsen where he won
            List<Game> carlsenWins = db.search()
                .withPlayer("Carlsen, Magnus")
                .withResult("1-0")
                .execute();

            System.out.println("Found " + carlsenWins.size() + " wins by Carlsen.");

            // Example 2: Find all Sicilian Najdorf games by players rated over 2700
            List<Game> najdorfGames = db.search()
                .withEco("B90")
                .withEloRange(2700, 3000)
                .execute();

            System.out.println("Found " + najdorfGames.size() + " high-level Najdorf games.");

            // Example 3: Count games without loading them (more efficient)
            int karpovGamesCount = db.search()
                .withPlayer("Karpov, Anatoly")
                .count();

            System.out.println("Anatoly Karpov played in " + karpovGamesCount + " games in this database.");
        }
    }
}
```

## Architecture

OrionDB is built with a layered architecture to separate concerns and allow for future expansion.

-   **Data Layer**: Manages the custom binary file format (`.oriondb`) and index file (`.oriondb.idx`).
-   **Parsing Layer**: Contains the high-performance, error-tolerant PGN parser.
-   **Domain Layer**: Defines the core data models like `Game` and `Move`.
-   **Index Layer**: Manages in-memory indexes for fast metadata lookups.
-   **Query Layer**: Provides the fluent `SearchBuilder` API and executes queries against the indexes.
-   **API Layer**: The public-facing `OrionDatabase` class that ties everything together.

## Future Roadmap

This initial version of OrionDB focuses on providing a solid foundation and a powerful metadata search API. Future development will focus on adding even more powerful search capabilities.

-   **Phase 2: Position & Structure Search**
    -   Implement Zobrist hashing for position indexing.
    -   Add search by FEN (Forsyth-Edwards Notation).
    -   Implement material balance and pawn structure search.

-   **Phase 3: Pro Features**
    -   Implement a full Chess Query Language (CQL) parser for single-string queries.
    -   Integrate Apache Lucene for full-text search on game commentary.

-   **Phase 4: Ecosystem**
    -   Create a standalone server to expose the API via REST/gRPC.
    -   Develop client libraries for other languages (e.g., Python, Node.js).

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue.

## License

OrionDB is licensed under the [MIT License](https://opensource.org/licenses/MIT).
