package com.mdp.caremate.ui.family.management

import androidx.lifecycle.*

import com.mdp.caremate.data.model.FamilyMember
import com.mdp.caremate.data.repositories.AuthRepository

import kotlinx.coroutines.launch

class FamilyManagementViewModel(

    private val repository: AuthRepository

) : ViewModel() {

    private val _familyMembers =
        MutableLiveData<List<FamilyMember>>()

    val familyMembers:
            LiveData<List<FamilyMember>>
        get() = _familyMembers

    private val _isPremiumRequired =
        MutableLiveData<Boolean>()

    val isPremiumRequired:
            LiveData<Boolean>
        get() = _isPremiumRequired

    fun loadFamilyMembers() {

        viewModelScope.launch {

            repository.getFamilyMemberList()
                .onSuccess {

                    _familyMembers.value = it
                }
        }
    }

}