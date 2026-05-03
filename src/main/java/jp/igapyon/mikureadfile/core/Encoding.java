package jp.igapyon.mikureadfile.core;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.EffectiveRequest;

public final class Encoding {
    private Encoding() {
    }

    public static String selectEncoding(EffectiveRequest request, EffectiveFileRequest file) {
        if (file.encoding != null) {
            return file.encoding;
        }
        String extension = extension(file.path);
        String selected = request.encoding.extensions.get(extension);
        return selected == null ? request.encoding.defaultEncoding : selected;
    }

    public static String decode(byte[] bytes, String encoding) throws CharacterCodingException {
        if ("utf-8".equals(encoding)) {
            CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
            decoder.onMalformedInput(CodingErrorAction.REPORT);
            decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
            CharBuffer decoded = decoder.decode(ByteBuffer.wrap(bytes));
            return decoded.toString();
        }
        return decodeShiftJis(bytes);
    }

    private static String decodeShiftJis(byte[] bytes) throws CharacterCodingException {
        CharsetDecoder decoder = Charset.forName("Shift_JIS").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes), decoder);
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        try {
            int read;
            while ((read = reader.read(buffer)) >= 0) {
                builder.append(buffer, 0, read);
            }
            return builder.toString();
        } catch (java.io.CharConversionException ex) {
            CharacterCodingException coding = new CharacterCodingException();
            coding.initCause(ex);
            throw coding;
        } catch (java.io.IOException ex) {
            CharacterCodingException coding = new CharacterCodingException();
            coding.initCause(ex);
            throw coding;
        }
    }

    private static String extension(String path) {
        int slash = path.lastIndexOf('/');
        String base = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = base.lastIndexOf('.');
        return dot >= 0 ? base.substring(dot) : "";
    }
}
