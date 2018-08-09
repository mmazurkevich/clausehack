package com.example.myapplication.users

data class UserDto(val firstName: String,
                   val lastName: String,
                   val email: String,
                   val username: String,
                   val password: String,
                   val passwordConfirm: String)

data class AuthorityDto(val authority: String)

data class UserUpdateDto(val firstName: String,
                   val lastName: String,
                   val email: String,
                   val username: String)