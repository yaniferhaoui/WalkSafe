files=$(find src -type f -name '*.java' | tr '\n' ' ')
if [ -n "$1" ]; then
    if [ $1 = "cr" ]; then
        mvn compile
        mvn exec:java #-Dexec.mainClass="com.yferhaoui.ebay_inventory_manager.UserWindow" -X
    elif [ $1 = "c" ]; then
        mvn compile
    elif [ $1 = "r" ]; then
        mvn exec:java #-Dexec.mainClass="com.yferhaoui.ebay_inventory_manager.UserWindow" -X
    else
        echo "error, set argument : 'cr' => compile and run | 'c' => compile | 'r' => run !"
    fi
else
    echo "error, set argument : 'cr' => compile and run | 'c' => compile | 'r' => run !"
fi
