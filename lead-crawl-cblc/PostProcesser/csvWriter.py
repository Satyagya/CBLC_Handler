import csv


def write(fileName, listOfDetails):
    """
    write the logging details in the csv file
    :param fileName:
    :param listOfDetails:
    :return: None
    """
    with open(fileName, 'a') as csvFile:
        csvwriter = csv.writer(csvFile)
        csvwriter.writerow(listOfDetails)
