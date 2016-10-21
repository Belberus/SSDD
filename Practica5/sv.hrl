%%%%
%%  Macros comunes a cliente y servidor del servicio de vistas
%%%%


%% macros que definen aspectos genericos
-define(INTERVALO_PING, 100).

-define(PINGS_FALLIDOS, 5).

-define(TIMEOUT, 50).

-define(PRINT(Texto,Datos), io:format(Texto,Datos)).
%-define(PRINT(Texto,Datos), ok)).

-define(ENVIO(Mensj, Dest),
        io:format("~p -> ~p -> ~p~n",[node(), Mensj, Dest]), Dest ! Mensj).
%-define(ENVIO(Mensj, Dest), Dest ! Mensj).

-define(ESPERO(Dato), Dato -> io:format("LLega ~p-> ~p~n",[Dato,node()]), ).
%-define(ESPERO(Dato), Dato -> ).

