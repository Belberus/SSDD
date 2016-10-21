# Compilar de forma sistematica a cada validacion
# para modificaciones de depuracion
./compilar_clave_valor.sh

# Ejecución del programa de tests
erl -connect_all false -noshell -name maestro@127.0.0.1 -rsh ssh \
     -setcookie 'palabrasecreta' \
    -eval "eunit:test(servicio_vistas_tests, [verbose])."  -run init stop

#Una vez terminada ejecución programa, eliminar demonio de conexiones red Erlang
epmd -kill
