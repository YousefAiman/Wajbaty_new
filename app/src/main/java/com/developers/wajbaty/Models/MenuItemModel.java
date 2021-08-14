package com.developers.wajbaty.Models;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.UUID;

public class MenuItemModel extends Observable {

    public static final int CART_RESULT = 1, HAS_IN_CART = 2, NOT_IN_CART = 3,
            ADD_TO_CART_SUCCESS = 4, ADD_TO_CART_FAILED = 6, REMOVE_FROM_CART_SUCCESS = 7, REMOVE_FROM_CART_FAILED = 8,
            CHECK_FAVORED_SUCCESS = 9, CHECK_FAVORED_FAILED = 10,
            FAVORING_SUCCESS = 11, FAVORING_FAILED = 12, UN_FAVORING_SUCCESS = 13, UN_FAVORING_FAILED = 14,
            REMOVE_SUCCESS = 15, REMOVE_FAILED = 16;
    public boolean hasInCart, isFavored;
    private MenuItem.MenuItemSummary menuItem;
    private FirebaseFirestore firestore;
    private CollectionReference userRef;

    public MenuItemModel() {

    }

    public MenuItemModel(MenuItem.MenuItemSummary menuItem) {
        this.menuItem = menuItem;
        firestore = FirebaseFirestore.getInstance();
    }

    public void setFavored(boolean favored) {
        isFavored = favored;
    }

    public MenuItem.MenuItemSummary getMenuItem() {
        return menuItem;
    }

    public void uploadMenuItem(String name, float price, String currency, String category,
                               List<Uri> images, ArrayList<String> ingredients, String restaurantId,
                               String region) {

        final String id = UUID.randomUUID().toString();


        final StorageReference storageReference =
                FirebaseStorage.getInstance().getReference().child(restaurantId);

        final List<UploadTask> uploadTasks = new ArrayList<>();
        final List<Task<Uri>> uriTasks = new ArrayList<>();

        final List<String> bannerImages = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {

            final UploadTask bannerUploadTask = storageReference.child(id).child("menuItemImage_" + i).putFile(images.get(i));

            bannerUploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        uriTasks.add(task.getResult().getStorage().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Log.d("uploadRestaurant", "finished getting uri");
                                        bannerImages.add(uri.toString());

                                    }
                                }));
                    }
                }
            });


            uploadTasks.add(bannerUploadTask);
        }

        final MenuItem.Builder builder = new MenuItem.Builder();
        builder.setID(id);
        builder.setName(name);
        builder.setCategory(category);
        builder.setPrice(price);
        builder.setCurrency(currency);
        builder.setRestaurantId(restaurantId);
        builder.setTimeCreated(System.currentTimeMillis());
        builder.setIngredients(ingredients);
        builder.setRegion(region);


        Tasks.whenAllSuccess(uploadTasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
            @Override
            public void onComplete(@NonNull Task<List<Object>> task) {

                Log.d("ttt", "uploading success");

                Tasks.whenAllSuccess(uriTasks).addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Object>> task) {

                        Log.d("ttt", "uri success");

                        builder.setImageUrls(bannerImages);

                        final MenuItem menuItem = builder.build();

                        final DocumentReference menuItemRef =
                                FirebaseFirestore.getInstance().collection("MenuItems")
                                        .document(menuItem.getID());

                        menuItemRef.set(menuItem)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        setChanged();
                                        notifyObservers(menuItem);

//                                        FirebaseFirestore.getInstance().collection("PartneredRestaurant")
//                                                .document(GlobalVariables.getCurrentRestaurantId())
//                                                .collection("MenuItems")
//                                                .document("MenuItems")
//                                                .update("MenuItems", FieldValue.arrayUnion(
//                                                        menuItemRef
//                                                )).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                            @Override
//                                            public void onSuccess(Void aVoid) {
//
//                                                    notifyObservers(menuItem);
//
//                                            }
//                                        }).addOnFailureListener(new OnFailureListener() {
//                                            @Override
//                                            public void onFailure(@NonNull Exception e) {
//                                                notifyObservers(e.getMessage());
//
//                                            }
//                                        });


                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                setChanged();
                                notifyObservers(e.getMessage());
                            }
                        });

//                        FirebaseFirestore.getInstance().collection("PartneredRestaurant")
//                                .document(GlobalVariables.getCurrentRestaurantId())
//                                .collection("MenuItems")
//                                .document(menuItem.getID())
//                                .set(menuItem)
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//
//                                        Log.d("ttt","adding menu item complete");
//
//                                        if(task.isSuccessful()){
//                                            notifyObservers(menuItem);
//                                        }else{
//                                            notifyObservers(task.getException().getMessage());
//
//                                            Log.d("ttt","adding item error: "+task.getException().getMessage());
//                                        }
//
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                notifyObservers(e.getLocalizedMessage());
//
//                                Log.d("ttt","adding item error: "+e.getMessage());
//
//                            }
//                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ttt", "uri errorr: " + e.getMessage());
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("ttt", "uploading errorr: " + e.getMessage());

            }
        });

//        Tasks.whenAllComplete(uploadTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//            @Override
//            public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//
//
//                Tasks.whenAllComplete(uriTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
//                    @Override
//                    public void onComplete(@NonNull Task<List<Task<?>>> task) {
//
//
//
//                    }
//                });
//
//            }
//        });

    }

//    private static void

    public void checkInUserCart(String userId) {

        getUserRef().document(userId).collection("Cart")
                .document(menuItem.getID())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot.exists()) {
                    hasInCart = true;
                    setChanged();
                    notifyObservers(HAS_IN_CART);

                } else {
                    hasInCart = false;
                    setChanged();
                    notifyObservers(NOT_IN_CART);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                notifyError(NOT_IN_CART, e.getMessage());

            }
        });

//        getUserRef().whereEqualTo("menuItem.getID()",userId)
//                .whereArrayContains("Cart",menuItem.getID())
//                .limit(1)
//                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot snapshots) {
//
//
//
//
//            }
//        });


    }

    public void addToOrRemoveFromCart(String userId) {

        final DocumentReference cartItemRef =
                getUserRef().document(userId).collection("Cart").document(menuItem.getID());

        if (!hasInCart) {
            cartItemRef.set(new CartItem(menuItem.getID(), 1, System.currentTimeMillis(), menuItem.getRestaurantId()))
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            getUserRef().document(userId).update("CartTotal",
                                    FieldValue.increment(menuItem.getPrice()))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            hasInCart = true;
                                            setChanged();
                                            notifyObservers(ADD_TO_CART_SUCCESS);


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    getUserRef().document(userId).delete();

                                    notifyError(ADD_TO_CART_FAILED, e.getMessage());
                                }
                            });

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    notifyError(ADD_TO_CART_FAILED, e.getMessage());

                }
            });
        } else {

            cartItemRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {

                    if (snapshot.exists()) {

                        final float price = snapshot.getLong("count") * menuItem.getPrice();

                        cartItemRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                getUserRef().document(userId).update("CartTotal",
                                        FieldValue.increment(-price)).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        hasInCart = false;
                                        setChanged();
                                        notifyObservers(REMOVE_FROM_CART_SUCCESS);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        cartItemRef.set(Objects.requireNonNull(snapshot.toObject(CartItem.class)));
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                notifyError(CART_RESULT, e.getMessage());

                            }
                        });

                    } else {

                        notifyError(CART_RESULT, "cart item doesn't exist");

                    }
                }
            });


        }


//
//                .update("Cart",hasInCart?FieldValue.arrayRemove(menuItem.getID()): FieldValue.arrayUnion(menuItem.getID()))
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//
//                        hasInCart = !hasInCart;
//
//                            setChanged();
//                            notifyObservers(hasInCart?REMOVE_FROM_CART_SUCCESS:ADD_TO_CART_SUCCESS);
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//
//                notifyError(hasInCart?REMOVE_FROM_CART_FAILED:ADD_TO_CART_FAILED,e.getMessage());
//
//            }
//        });

    }

    public void checkAlreadyFavItem(String userId) {

        getUserRef().document(userId).collection("Favorites")
                .whereArrayContains("FavoriteMenuItems", menuItem.getID())
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {

                        isFavored = snapshots != null && !snapshots.isEmpty();
                        setChanged();
                        notifyObservers(CHECK_FAVORED_SUCCESS);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                notifyError(CHECK_FAVORED_FAILED, e.getMessage());
            }
        });


    }

    public void favOrUnFavItem(String restaurantId, String userId) {

        getUserRef().document(userId).collection("Favorites")
                .document(restaurantId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot snapshot) {

                if (snapshot.exists()) {
                    updateFavForReference(snapshot.getReference());
                } else {

                    final HashMap<String, Object> favMap = new HashMap<>();
                    favMap.put("FavoriteMenuItems", new ArrayList<>());

                    snapshot.getReference().set(favMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            updateFavForReference(snapshot.getReference());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("ttt", "failed to add snapshot: " + e.getMessage());
                            notifyError(isFavored ? UN_FAVORING_FAILED : FAVORING_FAILED, e.getMessage());

                        }
                    });

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                notifyError(isFavored ? UN_FAVORING_FAILED : FAVORING_FAILED, e.getMessage());
            }
        });

    }

    private void updateFavForReference(DocumentReference ref) {

        ref.update("FavoriteMenuItems",
                isFavored ? FieldValue.arrayRemove(menuItem.getID()) : FieldValue.arrayUnion(menuItem.getID()))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        firestore.collection("MenuItems")
                                .document(menuItem.getID()).update("favoriteCount",
                                FieldValue.increment(isFavored ? -1 : 1));

                        final boolean wasFavored = isFavored;
                        isFavored = !isFavored;
                        setChanged();
                        notifyObservers(wasFavored ? UN_FAVORING_SUCCESS : FAVORING_SUCCESS);

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                notifyError(isFavored ? UN_FAVORING_FAILED : FAVORING_FAILED, e.getMessage());

            }
        });
    }

    public void deleteMenuItem() {

        final DocumentReference menuItemRef =
                firestore.collection("MenuItems").document(menuItem.getID());

        menuItemRef.update("isDeleted", true)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        final HashMap<Integer, Object> resultMap = new HashMap<>();

                        menuItemRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {


                                resultMap.put(REMOVE_SUCCESS, menuItem.getID());
                                setChanged();
                                notifyObservers(resultMap);
                                deleteObservers();

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                resultMap.put(REMOVE_FAILED, e.getMessage());
                                setChanged();
                                notifyObservers(resultMap);

                            }
                        });

                    }
                });

    }

    private void notifyError(int key, String error) {
        final HashMap<Integer, Object> resultMap = new HashMap<>();
        resultMap.put(key, error);
        setChanged();
        notifyObservers(resultMap);
    }

    private CollectionReference getUserRef() {

        if (userRef == null)
            userRef = firestore.collection("Users");

        return userRef;
    }
}
