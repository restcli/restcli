package uos.dev.restcli.parser;
%%

%public

%unicode
%state S_REQUEST_LINE, S_HEADER, S_BODY, S_SCRIPT_HANDLER, S_SCRIPT_REFERENCE

%{
private boolean hasRequestTarget = false;
private boolean isMultiplePart = false;
private void reset() {
    hasRequestTarget = false;
    isMultiplePart = false;
}

public boolean isMultiplePart() {
    return isMultiplePart;
}

private void throwError() throws ParserException {
    throw new ParserException("Error while parsing: " + yytext());
}
%}

%yylexthrow ParserException

INPUT_CHARACTER = [^\r\n]
DIGIT = [0-9]
ALPHA = [a-zA-Z]

// Line terminators.
NEW_LINE = \r|\n|\r\n
NEW_LINE_WITH_INTENT = {NEW_LINE}{WHITE_SPACE}+
LINE_TAIL = {INPUT_CHARACTER}*{NEW_LINE}

// White space.
WHITE_SPACE = [ \t\f]
OPTIONAL_WHITE_SPACE = {WHITE_SPACE}*
REQUIRED_WHITE_SPACE = {WHITE_SPACE}+

// Comments.
LINE_COMMENT = (#|\/\/){LINE_TAIL}

// Request separator.
REQUEST_SEPARATOR = ###{LINE_TAIL}

// Request.
METHOD = GET|HEAD|POST|PUT|DELETE|CONNECT|PATCH|OPTIONS|TRACE
HTTP_VERSION = HTTP\/{DIGIT}+\.{DIGIT}+

REQUEST_TARGET = [^\r\n\s]+
REQUEST_METHOD = {METHOD}{REQUIRED_WHITE_SPACE}
REQUEST_HTTP_VERSION = {REQUIRED_WHITE_SPACE}{HTTP_VERSION}

REQUEST_LINE_IMPLICIT_METHOD = {REQUEST_TARGET}({REQUIRED_WHITE_SPACE}{HTTP_VERSION})?
REQUEST_LINE_EXPLICIT_METHOD = {REQUEST_METHOD}{REQUEST_LINE_IMPLICIT_METHOD}
REQUEST_LINE = {REQUEST_LINE_EXPLICIT_METHOD}|{REQUEST_LINE_IMPLICIT_METHOD}

FIELD_NAME = [^\r\n:]+
FIELD_VALUE = {LINE_TAIL}({NEW_LINE_WITH_INTENT}{LINE_TAIL})*
HEADER_FIELD = {FIELD_NAME}:{OPTIONAL_WHITE_SPACE}{FIELD_VALUE}{OPTIONAL_WHITE_SPACE}

// Message body.
MESSAGE_LINE_TEXT = !(\r|\n|<|(<>)).*
FILE_PATH = {LINE_TAIL}
MESSAGE_LINE_FILE = <{REQUIRED_WHITE_SPACE}{FILE_PATH}

MULTIPLE_PART_BOUNDARY = --{LINE_TAIL}
// Response handler.
HANDLER_SCRIPT = (!(%}|###).)*
RESPONSE_HANDLER_EMBEDED_OPEN = >{REQUIRED_WHITE_SPACE}\{%
RESPONSE_HANDLER_EMBEDED_CLOSE = %\}
RESPONSE_HANDLER_SCRIPT = >{REQUIRED_WHITE_SPACE}{FILE_PATH}

// Response reference.
RESPONSE_REFERENCE = <>{REQUIRED_WHITE_SPACE}{FILE_PATH}

FALLBACK_CH = .
%%

<YYINITIAL>{
// Ignore white-space.
\s+                  {}
{REQUEST_SEPARATOR} {
          reset();
          yybegin(S_REQUEST_LINE);
          return new Yytoken(Yytoken.TYPE_SEPARATOR, yytext().trim());
      }
{LINE_COMMENT} { return new Yytoken(Yytoken.TYPE_COMMENT, yytext()); }
{FALLBACK_CH}		{ yypushback(1); yybegin(S_REQUEST_LINE); }
}

<S_REQUEST_LINE> {
{REQUEST_METHOD} { return new Yytoken(Yytoken.TYPE_REQUEST_METHOD, yytext().trim()); }
{REQUEST_TARGET} { hasRequestTarget = true; return new Yytoken(Yytoken.TYPE_VALUE, yytext()); }

{NEW_LINE}|{NEW_LINE_WITH_INTENT} { yybegin(S_BODY); }

{FALLBACK_CH} {
          if (!hasRequestTarget) throwError();
          yypushback(1);
          yybegin(S_HEADER);
      }
}

<S_HEADER> {
{HEADER_FIELD} { return new Yytoken(Yytoken.TYPE_VALUE, yytext().trim()); }
{NEW_LINE}|{NEW_LINE_WITH_INTENT} { yybegin(S_BODY); }
{LINE_COMMENT} { return new Yytoken(Yytoken.TYPE_COMMENT, yytext()); }
{FALLBACK_CH} { yypushback(1); yybegin(YYINITIAL); }
}

<S_BODY> {
{MESSAGE_LINE_TEXT} { return new Yytoken(Yytoken.TYPE_VALUE, yytext()); }
{MESSAGE_LINE_FILE} { return new Yytoken(Yytoken.TYPE_VALUE_FILE_REF, yytext()); }
{MULTIPLE_PART_BOUNDARY} { isMultiplePart = true; }
{REQUEST_SEPARATOR} { yypushback(1); yybegin(YYINITIAL); }
{LINE_COMMENT} { return new Yytoken(Yytoken.TYPE_COMMENT, yytext()); }
{FALLBACK_CH} { yypushback(1); yybegin(S_SCRIPT_HANDLER); }
}

<S_SCRIPT_HANDLER> {
{RESPONSE_HANDLER_SCRIPT} {
          yybegin(S_SCRIPT_REFERENCE);
          return new Yytoken(Yytoken.TYPE_VALUE_FILE_REF, yytext());
      }
{RESPONSE_HANDLER_EMBEDED_OPEN} { return new Yytoken(Yytoken.TYPE_OPEN_SCRIPT_HANDLER); }
{RESPONSE_HANDLER_EMBEDED_CLOSE} { return new Yytoken(Yytoken.TYPE_CLOSE_SCRIPT_HANDLER); }
{HANDLER_SCRIPT} { return new Yytoken(Yytoken.TYPE_VALUE, yytext()); }
{FALLBACK_CH} { yypushback(1); yybegin(S_SCRIPT_HANDLER); }
}

<S_SCRIPT_REFERENCE> {
{RESPONSE_REFERENCE} { return new Yytoken(Yytoken.TYPE_VALUE_FILE_REF, yytext()); }
{FALLBACK_CH} { yypushback(1); yybegin(YYINITIAL); }
}
