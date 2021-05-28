import mysql.connector
from mysql.connector import Error
from mysql.connector import errorcode
import config

def getProductsFromDB():
    data=[]
    connection = None
    try:
        connection = mysql.connector.connect(host=config.HOST,
                                             database=config.DATABASE,
                                             user=config.USERNAME,
                                             password=config.PASSWORD)

        mySql_select_query = config.MARIADB_SELECT_QUERY
        cursor = connection.cursor()
        cursor.execute(mySql_select_query)
        records = cursor.fetchall()
        for row in records:
            product_name = row[1]
            data.append(product_name)
        cursor.close()
        # data = list(set(data))
        # print(data)
    except mysql.connector.Error as error:
        print("Failed to insert record into Laptop table {}".format(error))

    finally:
        if connection!=None:
            if (connection.is_connected()):
                connection.close()
                print("MySQL connection is closed")

    return data
