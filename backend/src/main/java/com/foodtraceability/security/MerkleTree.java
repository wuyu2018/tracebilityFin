package com.foodtraceability.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public final class MerkleTree {

    private MerkleTree() {}

    public static String computeRoot(List<String> hashes) {
        if (hashes.isEmpty()) {
            return sha256Hex("");
        }
        if (hashes.size() == 1) {
            return hashes.get(0);
        }
        List<String> level = new ArrayList<>(hashes);
        while (level.size() > 1) {
            List<String> next = new ArrayList<>();
            for (int i = 0; i < level.size(); i += 2) {
                String left = level.get(i);
                String right = (i + 1 < level.size()) ? level.get(i + 1) : left;
                next.add(sha256Hex(left + right));
            }
            level = next;
        }
        return level.get(0);
    }

    static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
