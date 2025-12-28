package com.example.todoapp.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

object Validators {

    fun validateMobile(value: String): String? {
        return if (value.isEmpty()) "Mobile Number is Required" else null
    }

    fun validateName(value: String, type: String): String? {
        val pattern = Pattern.compile("^[a-zA-Z ]*$")
        return when {
            value.isEmpty() -> "$type is Required"
            !pattern.matcher(value).matches() -> "$type must be a-z and A-Z"
            else -> null
        }
    }

    fun validateDob(years: Int?): String? {
        return when {
            years == null -> "Date of birth is Required"
            years < 18 -> "Your age must be more than 18 years"
            else -> null
        }
    }

    fun validateEmail(value: String): String? {
        val pattern =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
        return when {
            value.isEmpty() -> "Email is Required"
            !pattern.matcher(value).matches() -> "Invalid Email"
            else -> null
        }
    }

    fun validateRequired(value: String, type: String): String? {
        return if (value.isEmpty()) "$type is Required" else null
    }

    fun validateCountryCode(value: String): String? {
        val pattern = Pattern.compile("^\\d{0,3}$")
        return when {
            value.isEmpty() -> "Country code is Required"
            !pattern.matcher(value).matches() -> "Country Code is not valid"
            else -> null
        }
    }

    fun validatePassword(value: String): String? {
        val pattern =
            Pattern.compile("^.*(?=.{8,})(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).*$")
        return when {
            value.isEmpty() -> "Password is Required"
            !pattern.matcher(value).matches() ->
                "Minimum 8 characters with uppercase, lowercase and number required"
            else -> null
        }
    }

    fun validateConfirmPassword(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isEmpty() -> "Confirm Password is Required"
            password != confirmPassword -> "Password & Confirm Password does not match"
            else -> null
        }
    }

    fun validateDate(value: String): String? {
        val pattern =
            Pattern.compile("([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))")
        return when {
            value.isEmpty() -> "Date is Required"
            !pattern.matcher(value).matches() -> "Please enter valid date"
            else -> null
        }
    }

    fun validateCardNumber(value: String): String? {
        return when {
            value.isEmpty() -> "Account Number is Required"
            value.length < 9 -> "Account Number is not valid"
            else -> null
        }
    }

    fun validateConfirmCardNumber(card: String, confirmCard: String): String? {
        return when {
            confirmCard.isEmpty() -> "Account Number is Required"
            card != confirmCard -> "Account Number does not match"
            else -> null
        }
    }

    fun validatePassengers(value: String, type: String): String? {
        return when {
            value.isEmpty() -> "$type is Required"
            value.toInt() >= 10 -> "Max $type limit is 10"
            else -> null
        }
    }

    fun isValidIFSC(value: String): String? {
        val pattern = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$")
        return when {
            value.isEmpty() -> "Please enter IFSC Code"
            !pattern.matcher(value).matches() -> "Please enter valid IFSC Code"
            else -> null
        }
    }

    fun hideDigits(number: String): String {
        return if (number.length <= 4) number
        else "•".repeat(number.length - 4) + number.takeLast(4)
    }

    // Capitalize first letter InputFilter
    val capitalizeFilter = InputFilter { source, start, end, dest, dstart, dend ->
        if (source.isNotEmpty()) {
            source.toString().replaceFirstChar { it.uppercaseChar() }
        } else source
    }

    fun getDocumentType(documentName: String): String {
        return when (documentName.substringAfterLast(".").lowercase()) {
            "pdf" -> "pdf"
            "doc", "docx" -> "doc"
            "jpg", "jpeg", "png" -> "image"
            else -> ""
        }
    }
}
