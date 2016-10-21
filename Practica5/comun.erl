%% ----------------------------------------------------------------------------
%% Modulo comun : Elementos comunes de cliente y servidor
%%
%%
%% ----------------------------------------------------------------------------

-module(comun).
-include_lib("eunit/include/eunit.hrl").

-export([vaciar_buzon/0]).


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Vaciar buzon de proceso en curso, tambien llamado flush() en shell Erlang
vaciar_buzon() ->
    receive _ -> vaciar_buzon()
    after   0 -> ok
    end.
