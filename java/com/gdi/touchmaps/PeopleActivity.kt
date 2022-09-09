package com.gdi.touchmaps

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.gdi.touchmaps.Interface.IRecyclerItemClickListener
import com.gdi.touchmaps.databinding.ActivityPeopleBinding
import com.gdi.touchmaps.utils.Common
import com.gdi.touchmaps.model.MyResponse
import com.gdi.touchmaps.model.Request
import com.gdi.touchmaps.model.User
import com.gdi.touchmaps.viewHolder.IFirebaseLoadDone
import com.gdi.touchmaps.viewHolder.UserViewHolder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mancj.materialsearchbar.MaterialSearchBar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import io.reactivex.schedulers.Schedulers

class PeopleActivity : AppCompatActivity(), IFirebaseLoadDone {

    private lateinit var binding:ActivityPeopleBinding
    var adapter: FirebaseRecyclerAdapter<User, UserViewHolder>?=null
    private var searchAdapter: FirebaseRecyclerAdapter<User, UserViewHolder>?=null
    lateinit var iFirebaseLoadDone:IFirebaseLoadDone
    var suggestList:List<String> = ArrayList()

    val compositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPeopleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val searchBar = binding.searchBar
        val peopleRecycler = binding.peopleRecycler
        //searchBar.elevation = 10F
        searchBar.setCardViewElevation(10)
        searchBar.addTextChangeListener(object:TextWatcher{
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
        searchBar.setOnSearchActionListener(object:MaterialSearchBar.OnSearchActionListener{
            override fun onSearchStateChanged(enabled: Boolean){
                if (!enabled)
                {
                    //close search
                    if (adapter !=null)
                        peopleRecycler.adapter = adapter
                }
            }

            override fun onSearchConfirmed(text: CharSequence?) {
                startSearch(text.toString())
            }

            override fun onButtonClicked(buttonCode: Int) {
                TODO("Not yet implemented")
            }
        })

        peopleRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(this)
        peopleRecycler.layoutManager = layoutManager
        peopleRecycler.addItemDecoration(DividerItemDecoration(this,layoutManager.orientation))

        iFirebaseLoadDone = this

        loadUserList()
        loadSearchData()
    }

    private fun startSearch(search_string:String)
    {
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
            .orderByChild("email")
            .startAt(search_string)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        searchAdapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user,parent,false)

                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if (model.email.equals(Common.loggedUser!!.email))
                {
                    holder.txt_user_email.text = StringBuilder(model.email!!).append(" (me) ")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface,Typeface.BOLD_ITALIC)
                }else{
                    holder.txt_user_email.text = StringBuilder(model.email!!)
                }

                holder.setClick(object :IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        showDialogRequest(model)
                    }
                })
            }

        }

        searchAdapter!!.startListening()
        binding.peopleRecycler.adapter = searchAdapter

    }
    private fun loadUserList(){
        val query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)

        val options = FirebaseRecyclerOptions.Builder<User>()
            .setQuery(query, User::class.java)
            .build()

        adapter = object:FirebaseRecyclerAdapter<User,UserViewHolder>(options)
        {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_user,parent,false)

                return UserViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {
                if (model.email.equals(Common.loggedUser!!.email))
                {
                    holder.txt_user_email.text = StringBuilder(model.email!!).append(" (me) ")
                    holder.txt_user_email.setTypeface(holder.txt_user_email.typeface,Typeface.BOLD_ITALIC)
                }else{
                    holder.txt_user_email.text = StringBuilder(model.email!!)
                }

                holder.setClick(object :IRecyclerItemClickListener{
                    override fun onItemClickListener(view: View, position: Int) {
                        if (model.email.equals(Common.loggedUser!!.email))
                            showMyDialog(model)
                        else
                            showDialogRequest(model)
                    }
                })
            }

        }

        adapter!!.startListening()
        binding.peopleRecycler.adapter = adapter

    }

    private fun showMyDialog(model: User) {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("My Info")
        alertDialog.setMessage("Email: "+model.email+"\nId: "+ model.uid)
        alertDialog.setIcon(R.drawable.ic_baseline_account_circle_24)
        alertDialog.setNegativeButton("Ok"){DialogInterface,_ -> DialogInterface.dismiss()}
        alertDialog.show()
    }

    private fun loadSearchData(){
        val lstUserEmail = ArrayList<String>()
        val userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
        userList.addListenerForSingleValueEvent(object:ValueEventListener{
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

    private fun showDialogRequest(model: User){
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Options")
        alertDialog.setMessage("Send friend request to "+model.email)
        alertDialog.setIcon(R.drawable.ic_baseline_account_circle_24)

        alertDialog.setPositiveButton("Send"){_,_ ->
            val acceptList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO)
                .child(Common.loggedUser!!.uid!!)
                .child(Common.ACCEPT_LIST)
            //check friend list
            acceptList.orderByKey().equalTo(model.uid!!)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null)
                            sendFriendRequest(model)
                        else
                            Toast.makeText(this@PeopleActivity,"Already in friend list",Toast.LENGTH_SHORT).show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@PeopleActivity,error.message,Toast.LENGTH_SHORT).show()
                    }
                })
        }
        alertDialog.setNegativeButton("Cancel"){dialogInterface,_-> dialogInterface.dismiss()}
        alertDialog.show()
    }

    private fun sendFriendRequest(model: User){
        val tokens = FirebaseDatabase.getInstance().getReference(Common.TOKENS)
        tokens.orderByKey().equalTo(model.uid)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null)
                        Toast.makeText(this@PeopleActivity,"Token error",Toast.LENGTH_SHORT).show()
                    else{
                        val request = Request()
                        val dataSend = HashMap<String,String>()
                        dataSend[Common.FROM_UID]= Common.loggedUser!!.uid!! // my uid
                        dataSend[Common.FROM_EMAIL] = Common.loggedUser!!.email!! // my email
                        dataSend[Common.TO_UID]= model.uid!! // friend uid
                        dataSend[Common.TO_EMAIL] = model.email!!// friend email

                        //set request
                        request.to = snapshot.child(model.uid!!).getValue(String::class.java)!!
                        request.data = dataSend

                        //send
                        compositeDisposable.add(Common.fcmService.sendFriendRequestToUser(request)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ t: MyResponse? ->
                                if (t!!.success == 1)
                                    Toast.makeText(this@PeopleActivity,"Request sent",Toast.LENGTH_SHORT).show()
                                if (t.failure == 1)
                                    Toast.makeText(this@PeopleActivity,"Request failed "+t.results+t.failure+t.canonical_ids+t.multicast_id,Toast.LENGTH_SHORT).show()
                            },{t:Throwable?->
                                Toast.makeText(this@PeopleActivity,t!!.message,Toast.LENGTH_SHORT).show()
                            })
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Snackbar.make(View(this@PeopleActivity),error.message,Snackbar.LENGTH_LONG).show()
                }

            })
    }

    override fun onStop() {
        if (adapter != null)
            adapter!!.stopListening()
        if (searchAdapter != null)
            searchAdapter!!.stopListening()
        super.onStop()
        compositeDisposable.clear()
    }


    override fun onFirebaseLoadUserDone(lstEmail: List<String>) {
        binding.searchBar.lastSuggestions = lstEmail
    }

    override fun onFirebaseLoadFailed(message: String) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
    }



}