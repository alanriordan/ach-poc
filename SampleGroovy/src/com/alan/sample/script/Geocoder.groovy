package com.alan.sample.script

import groovy.sql.Sql

def stadiums = []



stadiums <<
		new Stadium(name:'Angel Stadium',city:'Anaheim',state:'CA',team:'ana')
stadiums <<
		new Stadium(name:'Chase Field',city:'Phoenix',state:'AZ',team:'ari')

stadiums <<
		new Stadium(name:'Rogers Centre',city:'Toronto',state:'ON',team:'tor')
stadiums <<
		new Stadium(name:'Nationals Park',
		city:'Washington',state:'DC',team:'was')


def fillInLatLng(Stadium stadium) {
	def base = 'http://maps.googleapis.com/maps/api/geocode/xml?'
	def url = base + [sensor:false,
		address: [
			stadium.name,
			stadium.city,
			stadium.state
		].collect {
			URLEncoder.encode(it,'UTF-8')
		}.join(',')
	].collect {k,v -> "$k=$v"}.join('&')
	def response = new XmlSlurper().parse(url)
	stadium.latitude =
			response.result[0].geometry.location.lat.toDouble()
	stadium.longitude =
			response.result[0].geometry.location.lng.toDouble()
	return stadium
}

Sql db = Sql.newInstance(
    'jdbc:mysql://localhost:3306/baseball',
    'root',
    'root',
    'com.mysql.jdbc.Driver')

db.execute 'drop table  if exists stadium'
db.execute('''
    create table stadium(
        id int not null auto_increment,
        name varchar(200) not null,
        city varchar(200) not null,
        state char(2) not null,
        team char(3) not null,
        latitude double,
        longitude double,
        primary key(id)
    );
''')

Geocoder geo = new Geocoder()
stadiums.each { s ->
	this.fillInLatLng s
	db.execute """
        insert into stadium(name, city, state, team, latitude, longitude)
        values(${s.name},${s.city},${s.state},
               ${s.team},${s.latitude},${s.longitude});
    """
}






