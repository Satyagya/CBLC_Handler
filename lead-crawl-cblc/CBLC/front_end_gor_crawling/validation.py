def validate(df):
    header = ['id', 'Full name', 'Profile url', 'First name', 'Last name', 'Avatar', 'Title', 'Company', 'Position', 'Function', 'Size', 'Country']
    
    if header == df:
        return True

    else:
        return False


        