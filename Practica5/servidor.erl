%% ----------------------------------------------------------------------------
%% servidor : Modulo servidor de vistas
%%
%% ----------------------------------------------------------------------------

-module(servidor).
-include("sv.hrl").
-include_lib("eunit/include/eunit.hrl").

-export([start/2, stop/1]).

-export([init_sv/0, init_monitor/0]).

 %% Registro que guarda el estado del servidor de vistas
-record(estado_sv, {
		vistaValida::vista:vista(),
		vistaTentativa::vista:vista(),
	%%	nodos::[node()], %Lista con las @ de los nodos registrados [@nodo1, @nodo2, ...]
		pings::dict() %%Lista con tuplas {@nodo, nPingsFallados}

        %% Completar con lo campos necesarios para gestionar
        %% el estado del gestor de vistas   ---> MODIFICADO
                    }
        ).



%%%%%%%%%%%%%%%%%%%% Interface (FUNCIONES EXPORTABLES)  %%%%%%%%%%%%%%%%%%%%%%

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Poner en marcha el servicio de vistas con 2 procesos concurrentes
-spec start( atom(), atom() ) -> node().
start(Host, NombreNodo) ->
   % ?debugFmt("Arrancar un nodo servidor vistas~n",[]),

    %%%%% VUESTRO CODIGO DE INICIALIZACION
    
     % args para comando remoto erl
    Args = "-connect_all false -setcookie palabrasecreta",
        % arranca servidor en nodo remoto
    {ok, Nodo} = slave:start(Host, NombreNodo, Args),
  %  ?debugFmt("Nodo servidor vistas en marcha : ~p~n",[Nodo]),
    process_flag(trap_exit, true),
    spawn_link(Nodo, ?MODULE, init_sv, []),
    Nodo.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Parar nodo servidor de vista al completo, includos los 2 procesos
-spec stop( atom() ) -> ok.
stop(Nodo) ->
    slave:stop(Nodo),
    timer:sleep(10),
    comun:vaciar_buzon(),
    ok.
    


%%------------------------  FUNCIONES LOCALES  ------------------------------%%


%%-----------------------------------------------------------------------------
init_sv() ->
    register(sv, self()),
    spawn_link(?MODULE, init_monitor, []),
     
    %%%%% VUESTRO CODIGO DE INICIALIZACION AQUI    ---> MODIFICADO
    
    SV = #estado_sv{
		vistaValida = vista:vista_inicial(),
		vistaTentativa = vista:vista_inicial(),
	%%	nodos=[],
		pings=dict:new()
	},  
        
    bucle_recepcion(SV).
    
act_listaNodos(SV, NodoOrigen, 0) ->

	%%%%% VUESTRO CODIGO     --> MODIFICADO
           case vista:primario(SV#estado_sv.vistaValida) =:= undefined of
           true -> Primario = NodoOrigen,
				   SV2=SV#estado_sv{vistaTentativa=(vista:nueva_vista(vista:num_vista(SV#estado_sv.vistaTentativa)+1, Primario, vista:copia(SV#estado_sv.vistaTentativa)))},
				   SV3=SV2#estado_sv{vistaValida=SV2#estado_sv.vistaTentativa, pings=dict:store(NodoOrigen,0,SV2#estado_sv.pings)},
				   {cliente, NodoOrigen} ! {vista_tentativa, SV3#estado_sv.vistaTentativa, true},
				   SV3;
           false -> case vista:copia(SV#estado_sv.vistaValida) =:= undefined of
					true -> Copia = NodoOrigen,
				            SV2=SV#estado_sv{vistaTentativa=(vista:nueva_vista(vista:num_vista(SV#estado_sv.vistaTentativa)+1, vista:primario(SV#estado_sv.vistaTentativa), Copia))},
							SV3=SV2#estado_sv{vistaValida=SV2#estado_sv.vistaTentativa, pings=dict:store(NodoOrigen,0,SV2#estado_sv.pings)},
							{cliente, NodoOrigen} ! {vista_tentativa, SV3#estado_sv.vistaTentativa, true},
							?debugFmt("PONGO C2 EN COPIA --> ~w ~n",[vista:copia(SV3#estado_sv.vistaTentativa)]),
							SV3;
					false -> SV2=SV#estado_sv{pings=dict:store(NodoOrigen,0,SV#estado_sv.pings)},
							 {cliente, NodoOrigen} ! {vista_tentativa, SV2#estado_sv.vistaTentativa, true},
							 SV2
					end
		   end;

	    
act_listaNodos(SV, NodoOrigen, NumVista) ->
	SV2 = SV#estado_sv{pings=(dict:store(NodoOrigen, 0, SV#estado_sv.pings))},
	case NodoOrigen =:= vista:primario(SV#estado_sv.vistaTentativa) of
	false ->   {cliente,NodoOrigen} ! {vista_tentativa, SV#estado_sv.vistaTentativa, true},
			   SV;
	true ->
		case NumVista == vista:num_vista(SV#estado_sv.vistaTentativa) of
		true ->
			VistaTentativa = SV#estado_sv.vistaTentativa,
			SV2 = SV#estado_sv{vistaValida=VistaTentativa}, %%CAMBIO
			{cliente,NodoOrigen} ! {vista_valida, SV2#estado_sv.vistaValida, true},
  			SV2;
  		false ->
			SV
		end
	end.
	

%%-----------------------------------------------------------------------------
bucle_recepcion(SV) ->
	?debugFmt("EN BUCLE RECEPCION~n",[]), 

%%  Obtener nodoOrigen a partir de PID con node(Pid)
%% nodoOrigen --> @ cliente a la que se puede enviar
%% Pid --> Identificador de un proceso       
    receive
    
    %% ¿¿¿¿ DIVIDIR EN NumVista si 0 o si otro nº o mirarlo ???
    
        {ping, NodoOrigen, NumVista} ->
        
		?debugFmt("DENTRO DE --->> {ping, NodoOrigen, NumVista}~n",[]),
        
        SV2=act_listaNodos(SV, NodoOrigen, NumVista),
        bucle_recepcion(SV2);
	   	            
        {obten_vista, Pid} ->
		?debugFmt(" {obten_vista, Pid}~n",[]),

            %%%%% VUESTRO CODIGO     --> MODIFICADO
            
        %%¿¿Donde pone que la vista a devolver tiene que ser válida??. NO SE PUEDE HACER BUCLE WHILE
        %% EN ERLANG NO HAY BUCLES SOLO RECURSIVIDAD. SE PUEDE HACER:
			%% =/= --> exactly not equal to
			%%NodoEnvio =	node(Pid),
			Pid ! {vista_valida, SV#estado_sv.vistaValida},
			bucle_recepcion(SV);
        procesa_situacion_servidores -> 
		 ?debugFmt("procesa_situacion_servidores~n",[]),
	%%	 ?debugFmt("procesa_situacion_servidores -> ~w ~n",[vista:num_vista(SV#estado_sv.vistaValida)]),
            %%%%% VUESTRO CODIGO     --> MODIFICADO 
            
            %%Llama a procesar_situacion_servidores
            SV2=procesar_situacion_servidores(SV),
			bucle_recepcion(SV2)	
    end.
    

%%-----------------------------------------------------------------------------
init_monitor() ->
    sv ! procesa_situacion_servidores,
    timer:sleep(?INTERVALO_PING),
    init_monitor().
 
%------------------------------------------------------------------------------
nodoCaido([],SV) -> SV;
nodoCaido([X|L], SV) -> case dict:fetch(X,SV#estado_sv.pings) >= 5 of
					true ->
						SV2=SV#estado_sv{pings=(dict:erase(X, SV#estado_sv.pings))},
						nodoCaido(L,SV2);
					false -> nodoCaido(L,SV)
					end.

%%-----------------------------------------------------------------------------
getNodoEspera([], _P) -> undefined;
getNodoEspera([H|L], P) -> if H =/= P -> H;
						   true -> getNodoEspera(L,P)
						 end.

%%-----------------------------------------------------------------------------

copiaCaido(SV) ->
	case dict:is_key(vista:copia(SV#estado_sv.vistaTentativa),SV#estado_sv.pings) of
	true ->
		SV;
	false ->
		%%No esta el nodo copia --> HA CAIDO
		Keys = dict:fetch_keys(SV#estado_sv.pings),
		N = getNodoEspera(Keys, vista:primario(SV#estado_sv.vistaTentativa)), %%Obtengo nuevo nodo en espera
		case N =:= undefined of 
		true -> SV;
		false ->
			%%Actualizo vista tentativa
			SV2=SV#estado_sv{vistaTentativa=vista:nueva_vista(vista:num_vista(SV#estado_sv.vistaTentativa)+1, vista:primario(SV#estado_sv.vistaTentativa),N)},
			SV2
		end
	end.
	
%%-----------------------------------------------------------------------------

primarioCaido(SV)->
	case dict:is_key(vista:primario(SV#estado_sv.vistaTentativa),SV#estado_sv.pings) of
	true -> SV;
	false ->
		%%No esta el nodo copia --> HA CAIDO
		Primario = vista:copia(SV#estado_sv.vistaTentativa),
		Keys = dict:fetch_keys(SV#estado_sv.pings),
		Copia = getNodoEspera(Keys, Primario), %%Obtengo nuevo nodo en espera
		case Primario =:= undefined of 
		true -> SV;
		false ->
			%%Actualizo vista tentativa
			SV2=SV#estado_sv{vistaTentativa=vista:nueva_vista(vista:num_vista(SV#estado_sv.vistaTentativa)+1,Primario,Copia)},
			SV3=SV2#estado_sv{vistaValida=SV2#estado_sv.vistaTentativa},
			SV3
		end
	end.
	
actualizar_latidos([],1,Dict) ->
	Dict;
	
actualizar_latidos([X|L],1,Dict) ->
	Dict2 = dict:update_counter(X, 1,Dict),
	actualizar_latidos(L,1,Dict2).

%%----------------------------------------------------------------------------- 
procesar_situacion_servidores(SV) ->
	%%?debugFmt("EN PROCESAR SITUACION_SERVIDORES !! ~n",[]),
		%%Aumento el numero de pings a todos los nodos del diccionario "pings"
		Keys = dict:fetch_keys(SV#estado_sv.pings),
		Dict = SV#estado_sv.pings,
		Dict2 = actualizar_latidos(Keys,1,Dict),
		?debugFmt("DICT: ~w ~n",[dict:to_list(Dict2)]),

		SV2=SV#estado_sv{pings=Dict2},
               
		%%Elimino nodos que no han enviado ping tras 5 intervalos de ping
		SV3=nodoCaido(Keys, SV2),
				
		%%Mirar si copia ha caido
		SV4=copiaCaido(SV3),
				
		%%Miro si el nodo primario ha caido
		SV5=primarioCaido(SV4),
		SV5.
