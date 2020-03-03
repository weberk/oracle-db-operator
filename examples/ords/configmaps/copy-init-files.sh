if [ -f /opt/oracle/ords/config/ords/credentials ]; then
    echo "ords was already initialized..."
else
    cp credentials /opt/oracle/ords/config/ords
    cp cdbAdmin.properties /opt/oracle/ords/config/ords
    cp post-install.sh /opt/oracle/ords/config/ords
    chown -R 54321:54321 /opt/oracle/ords/config/ords
fi