package com.alan.sample.script

def server = new ServerSocket(8886);

server.accept { socket -> 
	socket.withStreams{ input, output -> 
		System.out.println 'Started server'
		output << "This is a test that alan wrote";
		
	}
}
