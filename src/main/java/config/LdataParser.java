package config;

import lombok.SneakyThrows;

import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.*;

/**
 * class for converting text files into maps of data.
 */
public class LdataParser {

    /**
     * Load the data from a specific file into a map.
     *
     * @param _file The file from which to load data.
     * @return The map of loaded data.
     */
    @SneakyThrows
    public static Map<String, Object> loadFrom(File _file) {
        return loadFrom(_file, false);
    }

    /**
     * Loads data from a file with the option of using byte literals.
     *
     * @param _file         The file from which to load data.
     * @param _byteLiterals If true, parses byte literals as well as ordinary data.
     * @return A Map containing the loaded data.
     */
    @SneakyThrows
    public static Map<String, Object> loadFrom(File _file, boolean _byteLiterals) {
        try (FileReader _fh = new FileReader(_file)) {
            return loadFrom(_fh, _byteLiterals);
        }
    }

    /**
     * Loads data from a Reader with the option of using byte literals.
     *
     * @param _file         The Reader from which to load data.
     * @param _byteLiterals If true, parses byte literals as well as ordinary data.
     * @return A map containing the loaded data.
     */
    @SneakyThrows
    public static Map<String, Object> loadFrom(Reader _file, boolean _byteLiterals) {
        try {
            CustomStreamTokenizer _tokener = new CustomStreamTokenizer(_file);
            _tokener.resetSyntax();
            _tokener.quoteChar('"');
            _tokener.tripleQuotes(true);
            _tokener.whitespaceChars(0, 32);
            _tokener.whitespaceChar(',', ';', ':', '=');
            _tokener.wordChars('a', 'z');
            _tokener.wordChars('A', 'Z');
            _tokener.wordChars('0', '9');
            _tokener.wordChar('_', '-', '@');
            _tokener.parseNumbers();
            _tokener.hexLiterals(true);
            _tokener.byteLiterals(_byteLiterals);
            _tokener.slashSlashComments(true);
            _tokener.slashStarComments(true);
            _tokener.commentChar('#');
            _tokener.commentChar('!');
            _tokener.commentChar('%');

            int _token = _tokener.nextToken();
            // check for unicode marker !
            if (_token == CustomStreamTokenizer.TOKEN_TYPE_WORD && _tokener.tokenAsString().charAt(0) == 0xfeff) {
                //ignore
            } else {
                _tokener.pushBack();
            }

            if (_token == '[' || _token == '(') {
                return Collections.singletonMap("data", readList(_tokener, _byteLiterals));
            }

            if (_token == '{') {
                _tokener.nextToken();
            }

            return readKeyValuePairs(_tokener, _byteLiterals);
        } finally {
            try {
                _file.close();
            } catch (Exception _xe) {
            }
        }
    }

    /**
     * Uses a custom stream tokenizer to parse key-value pairs from the data.
     *
     * @param _tokener      The CustomStreamTokenizer used for parsing the data.
     * @param _byteLiterals If true, parses byte literals as well as regular data.
     * @return A Map of String keys to Object values.
     * @throws IllegalArgumentException If duplicate keys are found or if an illegal key is encountered in the data.
     */
    @SneakyThrows
    static Map<String, Object> readKeyValuePairs(CustomStreamTokenizer _tokener, boolean _byteLiterals) {
        Map<String, Object> _ret = new LinkedHashMap<>();

        int _token;
        while ((_token = _tokener.nextToken()) != CustomStreamTokenizer.TOKEN_TYPE_EOF) {
            //System.out.println(String.format("T=%s n=%f t=%d s=%s", tokenToString(_token), _tokener.nval, _tokener.ttype, _tokener.sval));
            String _key = null;
            Object _val;
            if (_token == '}') {
                break;
            } else {
                switch (_token) {
                    case '"':
                    case CustomStreamTokenizer.TOKEN_TYPE_WORD: {
                        _key = _tokener.tokenAsString();
                        break;
                    }
                    case CustomStreamTokenizer.TOKEN_TYPE_NUMBER: {
                        _key = Long.toString((long) _tokener.tokenAsNumber());
                        break;
                    }
                    case CustomStreamTokenizer.TOKEN_TYPE_CARDINAL: {
                        _key = Long.toString(_tokener.tokenAsCardinal());
                        break;
                    }
                    default: {
                        break;
                    }
                }

                if (_key != null) {
                    _val = readValue(_tokener, _byteLiterals);

                    if (_ret.containsKey(_key)) {
                        throw new IllegalArgumentException(String.format("duplicate Key in line %d", _tokener.lineno()));
                    }
                    _ret.put(_key, _val);
                } else {
                    throw new IllegalArgumentException(String.format("Illegal Key in line %d", _tokener.lineno()));
                }
            }
        }
        return _ret;
    }

    /**
     * Reads an individual value from the stream tokenizer.
     *
     * @param _tokener      The CustomStreamTokenizer used for parsing the data.
     * @param _byteLiterals If true, parsers byte literals as well as regular data.
     * @return The parsed Object, the specific type depends on the data read from _tokener.
     * @throws IllegalArgumentException If any problems occur in parsing (exceptions from called read* methods).
     */
    @SneakyThrows
    static Object readValue(CustomStreamTokenizer _tokener, boolean _byteLiterals) {
        int _peek = _tokener.nextToken();
        Object _ret = null;
        switch (_peek) {
            case CustomStreamTokenizer.TOKEN_TYPE_BYTES:
                if (_byteLiterals) {
                    return _tokener.tokenAsBytes();
                }
            case '<':
                return readHereDoc(_tokener);
            case '"':
            case CustomStreamTokenizer.TOKEN_TYPE_WORD:
                return _tokener.tokenAsString();
            case CustomStreamTokenizer.TOKEN_TYPE_CARDINAL:
                return _tokener.tokenAsCardinal();
            case CustomStreamTokenizer.TOKEN_TYPE_NUMBER:
                return _tokener.tokenAsNumber();
            case '[':
            case '(':
                return readList(_tokener, _byteLiterals);
            case '{':
                return readKeyValuePairs(_tokener, _byteLiterals);
        }
        return _ret;
    }

    /**
     * Reads a Here Document from the source.
     *
     * @param _tokener The CustomStreamTokenizer from which to read the here document.
     * @return The here document as a String.
     * @throws IllegalArgumentException If the end of file is reached before the end of the here document.
     */
    @SneakyThrows
    static String readHereDoc(CustomStreamTokenizer _tokener) {
        StringBuilder _sb = new StringBuilder();
        int _lineno = _tokener.lineno();
        try {
            _tokener.readHereDocument("\n>>>\n", _sb);
        } catch (EOFException _eof) {
            throw new IllegalArgumentException(String.format("unfinished here document starting on line %d", _lineno));
        }

        return _sb.substring(_sb.toString().indexOf('\n') + 1);
    }

    /**
     * Reads a list of tokens from the source file.
     *
     * @param _tokener      The CustomStreamTokenizer from which to read the list.
     * @param _byteLiterals If true, parsers byte literals as well as regular data.
     * @return The list of tokens.
     * @throws IllegalArgumentException If an illegal token is encountered.
     */
    @SneakyThrows
    static List readList(CustomStreamTokenizer _tokener, boolean _byteLiterals) {
        List _ret = new Vector();

        int _token;
        while ((_token = _tokener.nextToken()) != CustomStreamTokenizer.TOKEN_TYPE_EOF) {
            switch (_token) {
                case ']':
                case ')':
                    return _ret;
                case CustomStreamTokenizer.TOKEN_TYPE_BYTES:
                    if (_byteLiterals) {
                        _ret.add(_tokener.tokenAsBytes());
                        break;
                    }
                case '<':
                    _ret.add(readHereDoc(_tokener));
                    break;
                case '[':
                case '(':
                    _ret.add(readList(_tokener, _byteLiterals));
                    break;
                case '{':
                    _ret.add(readKeyValuePairs(_tokener, _byteLiterals));
                    break;
                case '"':
                case CustomStreamTokenizer.TOKEN_TYPE_WORD:
                    _ret.add(_tokener.tokenAsString());
                    break;
                case CustomStreamTokenizer.TOKEN_TYPE_CARDINAL:
                    _ret.add(_tokener.tokenAsCardinal());
                    break;
                case CustomStreamTokenizer.TOKEN_TYPE_NUMBER:
                    _ret.add(_tokener.tokenAsNumber());
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Illegal Token '%s' in line %d", (char) _token, _tokener.lineno()));
            }
        }
        return _ret;
    }
}
