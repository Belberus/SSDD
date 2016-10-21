%% ----------------------------------------------------------------------------
%% Modulo vista : Implementación tipo abstracto vista
%%
%%
%% ----------------------------------------------------------------------------
-module(vista).
-include_lib("eunit/include/eunit.hrl").

-export([nueva_vista/3, vista_inicial/0, tupla/1]).

-export([primario/1, num_vista/1, copia/1]).

-export_type([vista/0]).

%% Tipo básico  de registro de vista que se maneja en el servicio de vistas
-record(vista, {num_vista :: integer(),
                primario :: node(),
                copia :: node() 
                }
        ).
                
-type vista() :: #vista{}.

%%%%%%%%%%%%%%%%%%%% INTERFACE (FUNCIONES EXPORTABLES)  %%%%%%%%%%%%%%%%%%%%%%

nueva_vista(NumVista, Primario, Copia) ->
    #vista{num_vista = NumVista, primario = Primario, copia = Copia}.

vista_inicial() ->
    #vista{num_vista = 0, primario = undefined, copia = undefined}.

primario(V) -> V#vista.primario.

num_vista(V) -> V#vista.num_vista.

copia(V) -> V#vista.copia.

tupla(V) -> {V#vista.num_vista, V#vista.primario, V#vista.copia}.
