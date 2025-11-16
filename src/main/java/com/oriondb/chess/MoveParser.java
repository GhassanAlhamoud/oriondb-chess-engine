package com.oriondb.chess;

/**
 * Parses Standard Algebraic Notation (SAN) moves.
 */
public class MoveParser {
    
    /**
     * Parsed move information.
     */
    public static class ParsedMove {
        public final int pieceType;      // Piece.WHITE_PAWN, etc.
        public final int toSquare;       // Destination square
        public final int fromFile;       // Source file (-1 if not specified)
        public final int fromRank;       // Source rank (-1 if not specified)
        public final boolean isCapture;
        public final boolean isCastleKingside;
        public final boolean isCastleQueenside;
        public final int promotionPiece; // Piece.NONE if no promotion
        public final boolean isCheck;
        public final boolean isMate;
        
        public ParsedMove(int pieceType, int toSquare, int fromFile, int fromRank,
                         boolean isCapture, boolean isCastleKingside, boolean isCastleQueenside,
                         int promotionPiece, boolean isCheck, boolean isMate) {
            this.pieceType = pieceType;
            this.toSquare = toSquare;
            this.fromFile = fromFile;
            this.fromRank = fromRank;
            this.isCapture = isCapture;
            this.isCastleKingside = isCastleKingside;
            this.isCastleQueenside = isCastleQueenside;
            this.promotionPiece = promotionPiece;
            this.isCheck = isCheck;
            this.isMate = isMate;
        }
    }
    
    /**
     * Parse a SAN move string.
     * 
     * Examples: "e4", "Nf3", "Bxe5", "O-O", "e8=Q", "Nbd7", "R1a3"
     */
    public static ParsedMove parse(String san, int sideToMove) {
        if (san == null || san.isEmpty()) {
            return null;
        }
        
        // Remove check/mate indicators
        boolean isCheck = san.contains("+");
        boolean isMate = san.contains("#");
        san = san.replace("+", "").replace("#", "").trim();
        
        // Check for castling
        if (san.equals("O-O") || san.equals("0-0")) {
            return new ParsedMove(Piece.NONE, Square.NONE, -1, -1, false, 
                                true, false, Piece.NONE, isCheck, isMate);
        }
        if (san.equals("O-O-O") || san.equals("0-0-0")) {
            return new ParsedMove(Piece.NONE, Square.NONE, -1, -1, false, 
                                false, true, Piece.NONE, isCheck, isMate);
        }
        
        int pos = 0;
        int pieceType = Piece.NONE;
        int fromFile = -1;
        int fromRank = -1;
        boolean isCapture = false;
        int promotionPiece = Piece.NONE;
        
        // Parse piece type
        char first = san.charAt(pos);
        if (first >= 'A' && first <= 'Z') {
            switch (first) {
                case 'N': pieceType = sideToMove == Piece.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT; break;
                case 'B': pieceType = sideToMove == Piece.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP; break;
                case 'R': pieceType = sideToMove == Piece.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK; break;
                case 'Q': pieceType = sideToMove == Piece.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN; break;
                case 'K': pieceType = sideToMove == Piece.WHITE ? Piece.WHITE_KING : Piece.BLACK_KING; break;
                default: return null; // Invalid piece
            }
            pos++;
        } else {
            // Pawn move
            pieceType = sideToMove == Piece.WHITE ? Piece.WHITE_PAWN : Piece.BLACK_PAWN;
        }
        
        // Parse disambiguation and destination
        // Format can be: e4, exd5, Nbd7, R1a3, Qh4e1
        
        // Look for capture indicator
        int capturePos = san.indexOf('x', pos);
        if (capturePos != -1) {
            isCapture = true;
            // Parse disambiguation before 'x'
            String beforeCapture = san.substring(pos, capturePos);
            if (beforeCapture.length() == 1) {
                char c = beforeCapture.charAt(0);
                if (c >= 'a' && c <= 'h') {
                    fromFile = c - 'a';
                } else if (c >= '1' && c <= '8') {
                    fromRank = c - '1';
                }
            } else if (beforeCapture.length() == 2) {
                fromFile = beforeCapture.charAt(0) - 'a';
                fromRank = beforeCapture.charAt(1) - '1';
            }
            pos = capturePos + 1;
        } else {
            // No capture, check for disambiguation
            // Look ahead to find destination square
            int destStart = -1;
            for (int i = pos; i < san.length() - 1; i++) {
                char c = san.charAt(i);
                char next = san.charAt(i + 1);
                if (c >= 'a' && c <= 'h' && next >= '1' && next <= '8') {
                    destStart = i;
                    break;
                }
            }
            
            if (destStart > pos) {
                // There's disambiguation
                String disambig = san.substring(pos, destStart);
                if (disambig.length() == 1) {
                    char c = disambig.charAt(0);
                    if (c >= 'a' && c <= 'h') {
                        fromFile = c - 'a';
                    } else if (c >= '1' && c <= '8') {
                        fromRank = c - '1';
                    }
                } else if (disambig.length() == 2) {
                    fromFile = disambig.charAt(0) - 'a';
                    fromRank = disambig.charAt(1) - '1';
                }
                pos = destStart;
            }
        }
        
        // Parse destination square
        if (pos + 1 >= san.length()) {
            return null; // Invalid move
        }
        
        char fileChar = san.charAt(pos);
        char rankChar = san.charAt(pos + 1);
        
        if (fileChar < 'a' || fileChar > 'h' || rankChar < '1' || rankChar > '8') {
            return null; // Invalid square
        }
        
        int toSquare = Square.fromAlgebraic("" + fileChar + rankChar);
        pos += 2;
        
        // Check for promotion
        if (pos < san.length() && san.charAt(pos) == '=') {
            pos++;
            if (pos < san.length()) {
                char promoPiece = san.charAt(pos);
                switch (promoPiece) {
                    case 'Q': promotionPiece = sideToMove == Piece.WHITE ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN; break;
                    case 'R': promotionPiece = sideToMove == Piece.WHITE ? Piece.WHITE_ROOK : Piece.BLACK_ROOK; break;
                    case 'B': promotionPiece = sideToMove == Piece.WHITE ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP; break;
                    case 'N': promotionPiece = sideToMove == Piece.WHITE ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT; break;
                }
            }
        }
        
        return new ParsedMove(pieceType, toSquare, fromFile, fromRank, isCapture,
                            false, false, promotionPiece, isCheck, isMate);
    }
}
