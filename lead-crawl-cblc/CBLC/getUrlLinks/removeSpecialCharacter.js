const removeAccents = require('remove-accents');

function removeSpecialCharaterForDomain(str) {
    
    str = removeAccents(str);
    str = str.replace(/[^a-zA-Z ]/g, '');
    str = str.replace(/[^\x20-\x7E]/g, '');

    return str;
}

function removeSpecialCharaterForLead(name,company,position)
{
    var str = ''
    name = removeAccents(name);
    name = name.replace(/[^a-zA-Z ]/g, '');
    name = name.replace(/[^\x20-\x7E]/g, '');

    position = removeAccents(position);
    position = position.replace(/[^a-zA-Z ]/g, "");
    position = position.replace(/[^\x20-\x7E]/g, '');

    company = removeAccents(company);
    company = company.replace(/[^a-zA-Z ]/g, "");
    company = company.replace(/[^\x20-\x7E]/g, '');
    str += 'site:linkedin.com/ AND ' + name + ' AND ' + company +' AND ' + position;
    return str;
}

module.exports = {
    removeSpecialCharaterForLead: removeSpecialCharaterForLead,
    removeSpecialCharaterForDomain: removeSpecialCharaterForDomain
    
}