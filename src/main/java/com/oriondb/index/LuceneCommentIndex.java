package com.oriondb.index;

import com.oriondb.model.GamePosition;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Full Apache Lucene integration for comment and annotation search.
 * Provides advanced text search capabilities including fuzzy matching,
 * phrase search, and boolean queries.
 */
public class LuceneCommentIndex implements AutoCloseable {
    private final Directory directory;
    private final StandardAnalyzer analyzer;
    private IndexWriter writer;
    private DirectoryReader reader;
    private IndexSearcher searcher;
    
    /**
     * Create or open a Lucene index.
     */
    public LuceneCommentIndex(File indexDir) throws IOException {
        this.directory = FSDirectory.open(indexDir.toPath());
        this.analyzer = new StandardAnalyzer();
        
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        
        this.writer = new IndexWriter(directory, config);
    }
    
    /**
     * Add a comment to the index.
     * 
     * @param gameId Game ID
     * @param moveNumber Move number (ply)
     * @param comment Comment text
     * @param annotation NAG annotation (e.g., "!!", "?", etc.)
     * @param fen FEN string of the position
     */
    public void addComment(int gameId, int moveNumber, String comment, 
                          String annotation, String fen) throws IOException {
        Document doc = new Document();
        
        // Stored fields (retrievable)
        doc.add(new IntPoint("gameId", gameId));
        doc.add(new StoredField("gameId", gameId));
        doc.add(new StoredField("moveNumber", moveNumber));
        doc.add(new StoredField("fen", fen));
        
        // Indexed fields (searchable)
        doc.add(new TextField("comment", comment != null ? comment : "", Field.Store.YES));
        doc.add(new StringField("annotation", annotation != null ? annotation : "", Field.Store.YES));
        
        // Combined field for general search
        String combined = (comment != null ? comment : "") + " " + (annotation != null ? annotation : "");
        doc.add(new TextField("all", combined, Field.Store.NO));
        
        writer.addDocument(doc);
    }
    
    /**
     * Commit all pending changes to the index.
     */
    public void commit() throws IOException {
        writer.commit();
        
        // Refresh reader and searcher
        if (reader != null) {
            DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
            if (newReader != null) {
                reader.close();
                reader = newReader;
                searcher = new IndexSearcher(reader);
            }
        } else {
            reader = DirectoryReader.open(directory);
            searcher = new IndexSearcher(reader);
        }
    }
    
    /**
     * Search comments using Lucene query syntax.
     * 
     * Examples:
     * - "novelty" - Simple term search
     * - "tactical sacrifice" - Multiple terms (AND by default)
     * - "novelty~2" - Fuzzy search (up to 2 edits)
     * - "\"theoretical novelty\"" - Phrase search
     * - "novelty OR sacrifice" - Boolean OR
     * - "novelty AND sacrifice" - Boolean AND
     * - "comment:novelty" - Field-specific search
     * 
     * @param queryString Lucene query string
     * @param maxResults Maximum number of results to return
     * @return List of matching game positions
     */
    public List<GamePosition> search(String queryString, int maxResults) throws Exception {
        if (searcher == null) {
            commit(); // Ensure searcher is initialized
        }
        
        QueryParser parser = new QueryParser("all", analyzer);
        parser.setDefaultOperator(QueryParser.Operator.AND);
        Query query = parser.parse(queryString);
        
        TopDocs topDocs = searcher.search(query, maxResults);
        
        List<GamePosition> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            int gameId = doc.getField("gameId").numericValue().intValue();
            int moveNumber = Integer.parseInt(doc.get("moveNumber"));
            String fen = doc.get("fen");
            
            results.add(new GamePosition(gameId, moveNumber, fen));
        }
        
        return results;
    }
    
    /**
     * Search for a specific annotation symbol.
     * 
     * @param annotation Annotation symbol (e.g., "!!", "?", "!?")
     * @param maxResults Maximum number of results
     * @return List of matching game positions
     */
    public List<GamePosition> searchByAnnotation(String annotation, int maxResults) throws IOException {
        if (searcher == null) {
            try {
                commit();
            } catch (IOException e) {
                return new ArrayList<>();
            }
        }
        
        Query query = new TermQuery(new Term("annotation", annotation));
        TopDocs topDocs = searcher.search(query, maxResults);
        
        List<GamePosition> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            int gameId = doc.getField("gameId").numericValue().intValue();
            int moveNumber = Integer.parseInt(doc.get("moveNumber"));
            String fen = doc.get("fen");
            
            results.add(new GamePosition(gameId, moveNumber, fen));
        }
        
        return results;
    }
    
    /**
     * Fuzzy search for a term.
     * 
     * @param term Search term
     * @param maxEdits Maximum edit distance (1 or 2)
     * @param maxResults Maximum number of results
     * @return List of matching game positions
     */
    public List<GamePosition> fuzzySearch(String term, int maxEdits, int maxResults) throws IOException {
        if (searcher == null) {
            try {
                commit();
            } catch (IOException e) {
                return new ArrayList<>();
            }
        }
        
        Query query = new FuzzyQuery(new Term("all", term), maxEdits);
        TopDocs topDocs = searcher.search(query, maxResults);
        
        List<GamePosition> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            int gameId = doc.getField("gameId").numericValue().intValue();
            int moveNumber = Integer.parseInt(doc.get("moveNumber"));
            String fen = doc.get("fen");
            
            results.add(new GamePosition(gameId, moveNumber, fen));
        }
        
        return results;
    }
    
    /**
     * Phrase search.
     * 
     * @param phrase Exact phrase to search for
     * @param maxResults Maximum number of results
     * @return List of matching game positions
     */
    public List<GamePosition> phraseSearch(String phrase, int maxResults) throws IOException {
        if (searcher == null) {
            try {
                commit();
            } catch (IOException e) {
                return new ArrayList<>();
            }
        }
        
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        String[] terms = phrase.toLowerCase().split("\\s+");
        for (String term : terms) {
            builder.add(new Term("all", term));
        }
        
        Query query = builder.build();
        TopDocs topDocs = searcher.search(query, maxResults);
        
        List<GamePosition> results = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            int gameId = doc.getField("gameId").numericValue().intValue();
            int moveNumber = Integer.parseInt(doc.get("moveNumber"));
            String fen = doc.get("fen");
            
            results.add(new GamePosition(gameId, moveNumber, fen));
        }
        
        return results;
    }
    
    /**
     * Get the number of documents in the index.
     */
    public int getDocumentCount() throws IOException {
        if (reader == null) {
            commit();
        }
        return reader.numDocs();
    }
    
    /**
     * Get statistics about the index.
     */
    public String getStats() throws IOException {
        int docCount = reader != null ? reader.numDocs() : 0;
        return String.format(
            "Lucene Comment Index Statistics:\n" +
            "  Indexed comments: %d",
            docCount
        );
    }
    
    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (directory != null) {
            directory.close();
        }
    }
}
