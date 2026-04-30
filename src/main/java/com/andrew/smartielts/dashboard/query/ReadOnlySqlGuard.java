package com.andrew.smartielts.dashboard.query;

import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class ReadOnlySqlGuard {

    private static final Pattern BLOCK_COMMENT = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
    private static final Pattern LINE_COMMENT = Pattern.compile("(?m)--[^\\r\\n]*$|#[^\\r\\n]*$");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    private static final Pattern WRITE_KEYWORDS = Pattern.compile(
            "\\b(insert|update|delete|replace|merge|upsert|drop|truncate|alter|create|rename|grant|revoke|call|execute|exec|set|use|commit|rollback|savepoint|release|lock|unlock)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern DANGEROUS_SELECT_PATTERNS = Pattern.compile(
            "\\b(into\\s+outfile|into\\s+dumpfile|load_file\\s*\\(|sleep\\s*\\(|benchmark\\s*\\(|for\\s+update|lock\\s+in\\s+share\\s+mode)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SYSTEM_SCHEMA_PATTERNS = Pattern.compile(
            "\\b(information_schema|performance_schema|mysql|sys)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SELECT_KEYWORD = Pattern.compile("\\bselect\\b", Pattern.CASE_INSENSITIVE);

    public void validate(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL cannot be blank");
        }

        String normalized = normalize(sql);
        validateSingleStatement(normalized);
        validateStartsWithReadOnlyClause(normalized);
        validateContainsSelect(normalized);
        validateNoWriteKeyword(normalized);
        validateNoDangerousReadPattern(normalized);
        validateNoSystemSchema(normalized);
    }

    private String normalize(String sql) {
        String withoutBlockComment = BLOCK_COMMENT.matcher(sql).replaceAll(" ");
        String withoutLineComment = LINE_COMMENT.matcher(withoutBlockComment).replaceAll(" ");
        String collapsed = MULTI_SPACE.matcher(withoutLineComment).replaceAll(" ").trim();
        return collapsed.toLowerCase(Locale.ROOT);
    }

    private void validateSingleStatement(String sql) {
        String candidate = stripTrailingSemicolon(sql);
        if (candidate.indexOf(';') >= 0) {
            throw new IllegalArgumentException("Only single-statement read-only SQL is allowed");
        }
    }

    private void validateStartsWithReadOnlyClause(String sql) {
        String candidate = stripTrailingSemicolon(sql).trim();
        if (!(candidate.startsWith("select ") || candidate.startsWith("with "))) {
            throw new IllegalArgumentException("Only SELECT or WITH read-only SQL is allowed");
        }
    }

    private void validateContainsSelect(String sql) {
        if (!SELECT_KEYWORD.matcher(sql).find()) {
            throw new IllegalArgumentException("Read-only SQL must contain SELECT");
        }
    }

    private void validateNoWriteKeyword(String sql) {
        if (WRITE_KEYWORDS.matcher(sql).find()) {
            throw new IllegalArgumentException("Write or administrative SQL keyword is not allowed");
        }
    }

    private void validateNoDangerousReadPattern(String sql) {
        if (DANGEROUS_SELECT_PATTERNS.matcher(sql).find()) {
            throw new IllegalArgumentException("Dangerous read-only SQL pattern is not allowed");
        }
    }

    private void validateNoSystemSchema(String sql) {
        if (SYSTEM_SCHEMA_PATTERNS.matcher(sql).find()) {
            throw new IllegalArgumentException("System schema access is not allowed");
        }
    }

    private String stripTrailingSemicolon(String sql) {
        String result = sql.trim();
        while (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).trim();
        }
        return result;
    }
}