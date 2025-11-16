# OrionDB Chess Database Engine

**OrionDB** is a high-performance, open-source chess database engine written in Java. It is designed for developers who need to build applications with rich, fast search functionality over large collections of chess games.

Unlike traditional chess applications, OrionDB is a library, not a GUI. It provides a powerful, developer-centric API to create, manage, and query chess databases programmatically.

## Key Features (Phase 0 & 1)

- **High-Performance PGN Parsing**: A fast, error-tolerant parser that can import millions of games from standard PGN files.
- **Optimized Binary Format**: A custom `.oriondb` binary format for compact storage and extremely fast read operations.
- **Rich Metadata Indexing**: Multi-layered indexes for all standard PGN tags, including player, event, ECO, date, Elo, and result.
- **Fluent Query API**: A clean, chainable, and type-safe API for building complex search queries.

## Phase 2 & 3 Features (New!)

- **Position & Structure Search**:
  - **Zobrist Hashing**: For efficient position identification.
  - **FEN Search**: Find games by exact board position.
  - **Material Indexing**: Search by material balance and imbalance.
  - **Pawn Structure Classification**: Identify and search for common pawn structures (IQP, Maroczy Bind, etc.).

- **CQL & Annotation Search**:
  - **Chess Query Language (CQL)**: A powerful single-string query interface.
  - **Full-Text Search**: On game commentary using a simplified index (full Lucene integration planned).

- **Minimal Dependencies**: Built with the Java 11+ standard library, with Apache Lucene for full-text search.
- **Open Source**: Licensed under the MIT License.

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

### 2. Loading and Querying a Database (Fluent API)

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

### 3. Querying with Chess Query Language (CQL)

For more complex or dynamic queries, you can use the powerful Chess Query Language (CQL) interface.

```java
// Example: Find all of Kasparov's wins with the King's Indian Defense in the 1990s
List<Game> kasparovGames = db.query(
    "player='Kasparov, Garry' AND eco='E97' AND result='1-0' AND date >= '1990.01.01' AND date <= '1999.12.31'"
);

// Example: Find games with a specific commentary note
List<Game> noveltyGames = db.query("commentary CONTAINS 'novelty'");
```

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

## Advanced Features

### Full-Text Search with Apache Lucene

OrionDB now integrates Apache Lucene for powerful full-text search on game commentary and annotations. This enables:

- **Fuzzy Search**: Find terms with typos (e.g., `novelty~2`)
- **Phrase Search**: Find exact phrases (e.g., `"theoretical novelty"`)
- **Boolean Logic**: Combine terms with AND, OR, NOT
- **Wildcards**: Use `*` and `?` for flexible matching
- **Field-Specific Queries**: Search within comments or annotations (`comment:brilliant`)

### Position Indexing and Search

When enabled, OrionDB will now index every position in every game, allowing you to search by:

- **FEN String**: Find all games that reached a specific position.
- **Material Balance**: Query for games with a certain material advantage or disadvantage.
- **Pawn Structure**: Search for common pawn structures like IQP, Maroczy Bind, and more.

### Extended Chess Query Language (CQL)

CQL has been extended to support these new features:

```java
// Find games with a specific position
db.query("fen=\'r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 2\'");

// Find games with a specific pawn structure
db.query("structure=\'IQP\'");

// Find games with commentary containing "brilliant sacrifice"
db.query("commentary CONTAINS \'brilliant sacrifice\'");

// Find games with a brilliant move annotation
db.query("annotation=\'!!\'");

// Find games where the move Nf3 was played
db.query("move=\'Nf3\'");
```

## Future Roadmap

This version of OrionDB implements the core features of Phases 0, 1, 2, and 3. Future development will focus on refining these features and expanding the ecosystem.

-   **Full Lucene Integration**: Enhance the commentary search with the full power of Apache Lucene, including fuzzy search, phrase search, and relevance scoring.

-   **Performance Optimization**: Further optimize the position and material indexes for even faster query performance and lower memory usage.

-   **Phase 4: Ecosystem**
    -   Create a standalone server to expose the API via REST/gRPC.
    -   Develop client libraries for other languages (e.g., Python, Node.js).

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
