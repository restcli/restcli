package uos.dev.restcli.parser;
%%

%public

%unicode
%state SCRIPT_HANDLER

%{
    // Java code
%}

%yylexthrow ParserException

%%

<YYINITIAL> ###.* { return new Yytoken(Yytoken.TYPE_SEPARATOR, yytext()); }

<SCRIPT_HANDLER> . {}
