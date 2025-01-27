package eu.faircode.xlua.tools;


public class BytesReplacer {
    private byte[] searchBytes;
    private byte[] replacementBytes;

    /**
     * Constructor for BytesReplacer.
     * @param searchBytes the byte array to find within another byte array.
     * @param replacementBytes the byte array to replace with where searchBytes are found.
     */
    public BytesReplacer(byte[] searchBytes, byte[] replacementBytes) {
        this.searchBytes = searchBytes;
        this.replacementBytes = replacementBytes;
    }

    /**
     * Replaces occurrences of searchBytes within a target byte array with replacementBytes.
     * @param targetBytes the byte array to modify.
     * @return the modified byte array with replacements.
     */
    public byte[] replace(byte[] targetBytes) {
        int index = indexOf(targetBytes, searchBytes);
        while (index != -1) {
            // Replace the found sequence
            targetBytes = replaceAt(targetBytes, index, searchBytes.length, replacementBytes);
            // Proceed to next possible match
            index = indexOf(targetBytes, searchBytes, index + replacementBytes.length);
        }
        return targetBytes;
    }

    /**
     * Finds the first occurrence of a byte array within another byte array from a specified index.
     * @param array the array to search in.
     * @param subArray the array to search for.
     * @param startIndex the index to start the search from.
     * @return the starting index of the subArray within array, or -1 if not found.
     */
    private int indexOf(byte[] array, byte[] subArray, int startIndex) {
        for (int i = startIndex; i < array.length - subArray.length + 1; i++) {
            boolean found = true;
            for (int j = 0; j < subArray.length; j++) {
                if (array[i + j] != subArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds the first occurrence of a byte array within another byte array.
     * @param array the array to search in.
     * @param subArray the array to search for.
     * @return the starting index of the subArray within array, or -1 if not found.
     */
    private int indexOf(byte[] array, byte[] subArray) {
        return indexOf(array, subArray, 0);
    }

    /**
     * Replaces a part of the byte array starting at the specified index with another byte array.
     * @param original the original byte array.
     * @param index the index to start replacement.
     * @param length the number of bytes in the original array to replace.
     * @param replacement the byte array to insert.
     * @return a new byte array with the replacement done.
     */
    private byte[] replaceAt(byte[] original, int index, int length, byte[] replacement) {
        byte[] newBytes = new byte[original.length - length + replacement.length];
        System.arraycopy(original, 0, newBytes, 0, index);
        System.arraycopy(replacement, 0, newBytes, index, replacement.length);
        System.arraycopy(original, index + length, newBytes, index + replacement.length, original.length - index - length);
        return newBytes;
    }
}