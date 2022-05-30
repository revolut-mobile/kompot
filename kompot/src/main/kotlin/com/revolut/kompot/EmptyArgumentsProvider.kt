package com.revolut.kompot

object EmptyArguments : FeatureInitialisationArgs

val emptyArgumentsProvider: () -> EmptyArguments = { EmptyArguments }