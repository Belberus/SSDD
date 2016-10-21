%% ----------------------------------------------------------------------------
%% servidor : Modulo cliente del servicio de vistas
%%              - la API contiene tanto interface para réplicas
%%              - como clientes del servicio de réplicas
%% ----------------------------------------------------------------------------

-module(cliente).
-include("sv.hrl").
-include_lib("eunit/include/eunit.hrl").

-export([start/3, stop/1, ping/2, obten_vista/1, primario/1]).

-export([init/1]).


%%%%%%%%%%%%%%%%%%%% INTERFACE (FUNCIONES EXPORTABLES)  %%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Poner en marcha un nodo cliente del servicio de vistas
-spec start(  atom(), atom(), atom() ) -> node().
start(Host, NombreNodo, ServidorVistas) ->
  %  ?debugFmt("Arrancar un nodo cliente~n",[]),

    %%%%% VUESTRO CODIGO DE INICIALIZACION
    
     % args para comando remoto erl
    Args = "-connect_all false -setcookie palabrasecreta",
        % arranca servidor en nodo remoto
    {ok, Nodo} = slave:start(Host, NombreNodo, Args),
  %  ?debugFmt("Nodo esclavo cliente en marcha : ~p~n",[Nodo]),
    process_flag(trap_exit, true),
    spawn_link(Nodo, ?MODULE, init, [ServidorVistas]),
    Nodo.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% parar un nodo cliente de servidor de vistas
-spec stop( atom() ) -> ok.
stop(Nodo) ->
    slave:stop(Nodo),
    timer:sleep(10),
    comun:vaciar_buzon(),
    ok.
    


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Indicar al cliente que envie un ping al servidor de vistas
-spec ping( node(), integer() ) -> vista:vista().
ping(NodoCliente, NumVista) ->
    {cliente, NodoCliente} ! {ping, NumVista, self()},
 
    receive % Esperar respuesta del ping
        {vista_tentativa, Vista, Is_ok} -> {Vista, Is_ok}
    after ?TIMEOUT ->
        ?debugFmt("~p : TIMEOUT ping en cliente = ~p ~n",[node(),NodoCliente]),       
        {vista:vista_inicial(), false}
    end.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% Indicar al cliente que envie una petición de obtención de vista válida
-spec obten_vista( node() ) -> {vista:vista(), boolean() }.
obten_vista(NodoCliente) ->
    {cliente, NodoCliente} ! {obten_vista, self()},
    
    receive % esperar respuesta de obten_vista
        {vista_valida, Vista, Is_ok} ->
            ?debugFmt("~p : obten vista ok en cliente  = ~p ~n",[node(),NodoCliente]),
            {Vista, Is_ok}
    after ?TIMEOUT ->
            ?debugFmt("~p : obten vista en cliente TIMEOUT = ~p ~n",[node(),NodoCliente]),
            {vista:vista_inicial(), false}
    end.



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
-spec primario(node()) -> node().
primario(NodoCliente) ->
    Resultado = obten_vista(NodoCliente),
    case Resultado of
        {Vista, true} ->  vista:primario(Vista);
        
        {_Vista, false} -> undefined
    end.


%%%%%%%%%%%%%%%%%  FUNCIONES LOCALES  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


%%-----------------------------------------------------------------------------
init(Servidor) ->
    register(cliente, self()),
    bucle_recepcion(Servidor).
    
    
%%-----------------------------------------------------------------------------
bucle_recepcion(Servidor) ->
    receive
        {ping, NumVista, PidMaestro} ->
        ?debugFmt("~p : recibe ping ~p ~n",[node(),{ping, NumVista, PidMaestro}]),       
            procesa_ping(Servidor, NumVista, PidMaestro),
            bucle_recepcion(Servidor);
        
        {obten_vista, PidMaestro} ->
            procesa_obten_vista(Servidor, PidMaestro),
            bucle_recepcion(Servidor)
    
    end.


%%-----------------------------------------------------------------------------
procesa_ping(Servidor, NumVista, PidMaestro) ->
    {sv, Servidor} ! {ping, node(), NumVista},
    
    receive 
        {vista_tentativa, Vista, HaSidoEncontrado} ->
            PidMaestro ! {vista_tentativa, Vista, HaSidoEncontrado}
    after ?TIMEOUT ->  
        ?debugFmt("~p : TIMEOUT ping con SV = ~p ~n",[node(),Servidor]),       
        PidMaestro ! {vista_tentativa, vista:vista_inicial(), false}
    end.


%%-----------------------------------------------------------------------------
procesa_obten_vista(Servidor, PidMaestro) ->
    {sv, Servidor} ! {obten_vista, self()},
    
    receive 
        {vista_valida, Vista} ->
     ?debugFmt("procesa obten vista en cliente vista valida = ~p~n",[Vista]),
           PidMaestro ! {vista_valida, Vista, true}
    after ?TIMEOUT ->  
     ?debugFmt("procesa obten vista en cliente TIMEOUT~n",[]),
        PidMaestro ! {vista_valida, vista:vista_inicial(), false}
    end.
