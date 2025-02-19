package com.example.grpc_server_demo

import com.example.grpc_server_demo.service.ArticleServiceImpl
import io.grpc.Server
import io.grpc.ServerBuilder
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class GrpcServerDemoApplication {
	@Bean
	fun grpcServer(): CommandLineRunner {
		return CommandLineRunner {
			val server: Server = ServerBuilder.forPort(9090)
				.addService(ArticleServiceImpl())
				.build()

			server.start()
			println("gRPC Server started on port 9090")
			server.awaitTermination()
		}
	}
}




fun main(args: Array<String>) {
	runApplication<GrpcServerDemoApplication>(*args)
}
