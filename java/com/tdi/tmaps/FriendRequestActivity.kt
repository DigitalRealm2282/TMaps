package com.tdi.tmaps

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.tdi.tmaps.databinding.ActivityFriendRequestBinding
import com.tdi.tmaps.utils.Common
import com.tdi.tmaps.model.User
import com.tdi.tmaps.viewHolder.FriendRequestViewHolder
import com.tdi.tmaps.viewHolder.IFirebaseLoadDone
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar

class FriendRequestActivity : AppCompatActivity(), IFirebaseLoadDone {

    private lateinit var binding: ActivityFriendRequestBinding
    var adapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>?=null
    private var searchAdapter: FirebaseRecyclerAdapter<User, FriendRequestViewHolder>?=null
    lateinit var iFirebaseLoadDone: IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()
    private lateinit var resource: Resources
    private lateinit var prefCurrentLang: SharedPreferences
    var context: Context? = null
    var text =  ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendRequestBinding.inflate(layoutInflater)

        setContentView(binding.root)

        prefCurrentLang = getSharedPreferences("currentLang", MODE_PRIVATE)

        resource = resources
        if (prefCurrentLang.getString("myLang", "en")=="en"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "en")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "en"
        }else if (prefCurrentLang.getString("myLang", "ar")=="ar"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "ar")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "ar"

        }else if (prefCurrentLang.getString("myLang", "fr")=="fr"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "fr")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "fr"

        }else if (prefCurrentLang.getString("myLang", "ja")=="ja"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "ja")
            resource = context!!.resources
            text = "ja"

        }else if (prefCurrentLang.getString("myLang", "zh")=="zh"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "zh")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "zh"

        }else if (prefCurrentLang.getString("myLang", "ms")=="ms"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "ms")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "ms"

        }else if (prefCurrentLang.getString("myLang", "ru")=="ru"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "ru")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "ru"

        }else if (prefCurrentLang.getString("myLang", "es")=="es"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "es")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "es"

        }else if (prefCurrentLang.getString("myLang", "de")=="de"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "de")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "de"

        }else if (prefCurrentLang.getString("myLang", "it")=="it"){
            context = LocaleHelper.setLocale(this@FriendRequestActivity, "it")
            resource = context!!.resources
            title = resource.getString(R.string.friend_req)

            text = "it"

        }

        val searchBar = binding.searchBar
        val friendRecycler = binding.friendRecycler

        searchBar.setCardViewElevation(10)
        searchBar.addTextChangeListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val suggest = ArrayList<String>()
                for (search in suggestList)
                    if (search.lowercase().contentEquals(searchBar.text.lowercase())) suggest.add(search)

                searchBar.lastSuggestions = suggest

            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        searchBar.setOnSearchActionListener(object: MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean){
                if (!enabled)
                {
                    //close search
                    if (adapter !=null)
                        friendRecycler.adapter = adapter
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

            override fun onButtonClicked(buttonCode: Int) {
                TODO("Not yet implemented")
            }
        })

        friendRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        friendRecycler.layoutManager = layoutManager
        friendRecycler.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))

        iFirebaseLoadDone = this


        loadFriendRequestList()
        loadSearchData()
    }

    private fun loadSearchData() {
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)


        userList.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children){
                    val user = userSnapshot.getValue(User::class.java)
                    lstUserEmail.add(user!!.email!!)
                }
                iFirebaseLoadDone.onFirebaseLoadUserDone(lstUserEmail)
            }

            override fun onCancelled(error: DatabaseError) {
                iFirebaseLoadDone.onFirebaseLoadFailed(error.message)
            }
        })
    }

    private fun startSearch(search_string:String)
    {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object:FirebaseRecyclerAdapter<User, FriendRequestViewHolder>(options)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_friend_request,parent,false)

                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int, model: User) {
                holder.friend_user_email.text = model.email
                holder.btn_decline.setOnClickListener {
                    deleteFriendRequest(model,true)
                }
                holder.btn_accept.setOnClickListener {
                    deleteFriendRequest(model,false)
                    addToAcceptList(model) // add your friend tou your friendList
                    addUserToFriendContact(model) // add you tou your friend friendList
                }
            }

        }

        searchAdapter!!.startListening()
        binding.friendRecycler.adapter = searchAdapter

    }

    private fun loadFriendRequestList() {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object :FirebaseRecyclerAdapter<User,FriendRequestViewHolder>(options){
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): FriendRequestViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_friend_request,parent,false)

                return FriendRequestViewHolder(itemView)
            }

            override fun onBindViewHolder(
                holder: FriendRequestViewHolder,
                position: Int,
                model: User
            ) {
                holder.friend_user_email.text = model.email
                holder.btn_decline.setOnClickListener {
                    deleteFriendRequest(model,true)
                }
                holder.btn_accept.setOnClickListener {
                    deleteFriendRequest(model,false)
                    addToAcceptList(model) // add your friend tou your friendList
                    addUserToFriendContact(model) // add you tou your friend friendList
                }
            }

        }
        adapter!!.startListening()
        binding.friendRecycler.adapter = adapter
    }

    private fun addUserToFriendContact(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(model.uid!!)
            .child(Common.ACCEPT_LIST)
        acceptList.child(Common.loggedUser!!.uid!!).setValue(Common.loggedUser)
    }

    private fun addToAcceptList(model: User) {
        val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.ACCEPT_LIST)
        acceptList.child(model.uid!!).setValue(model)
    }

    private fun deleteFriendRequest(model: User, isShowMessage: Boolean) {
        val friendRequest = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .child(Common.loggedUser!!.uid!!)
            .child(Common.FRIEND_REQUEST)

        friendRequest.child(model.uid!!).removeValue()
            .addOnSuccessListener {
                if (isShowMessage)
                    Toast.makeText(this,resource.getString(R.string.removed),Toast.LENGTH_SHORT).show()
            }
    }


    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        binding.searchBar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    }

    override fun onStop() {
        if (adapter != null)
            adapter!!.stopListening()
        if (searchAdapter != null)
            searchAdapter!!.stopListening()
        super.onStop()
    }
}