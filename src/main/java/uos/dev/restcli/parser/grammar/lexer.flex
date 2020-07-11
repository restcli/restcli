package uos.dev.restcli.parser.grammar;

%%

%public

%unicode

%{
  private String name;
%}

%%

"name " [a-zA-Z]+  {
          name = yytext().substring(5);
      }
[Hh] "ello"        { System.out.print(yytext()+" "+name+"!"); }
