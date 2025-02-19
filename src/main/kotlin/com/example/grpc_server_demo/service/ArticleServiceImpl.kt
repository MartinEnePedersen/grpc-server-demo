package com.example.grpc_server_demo.service

import com.example.grpc.ArticleOuterClass.Article
import com.example.grpc.ArticleOuterClass.ArticleRequest
import com.example.grpc.ArticleOuterClass.ArticleResponse
import com.example.grpc.ArticleServiceGrpc
import io.grpc.stub.StreamObserver
import org.springframework.stereotype.Service

@Service
class ArticleServiceImpl : ArticleServiceGrpc.ArticleServiceImplBase() {

    private val mockedArticles = listOf(
        Article.newBuilder().setId(1).setTitle("Kotlin Basics").setContent("Learn the basics of Kotlin").build(),
        Article.newBuilder().setId(2).setTitle("Spring Boot with Kotlin").setContent("How to build a Spring Boot app using Kotlin").build(),
        Article.newBuilder().setId(3).setTitle("gRPC Introduction").setContent("Getting started with gRPC in Java/Kotlin").build()
    )

    override fun getArticles(request: ArticleRequest, responseObserver: StreamObserver<ArticleResponse>) {
        val limit = request.limit.takeIf { it > 0 } ?: 10
        val articles = mockedArticles.take(limit)

        val response = ArticleResponse.newBuilder()
            .addAllArticles(articles)
            .build()

        responseObserver.onNext(response)
        responseObserver.onCompleted()
    }
}
