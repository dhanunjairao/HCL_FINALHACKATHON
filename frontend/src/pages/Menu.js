import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useCart } from '../context/CartContext';

export default function Menu() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [restaurant, setRestaurant] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const { addToCart, items: cartItems, restaurantId: cartRestaurantId } = useCart();

  useEffect(() => {
    Promise.all([
      api.get(`/restaurants/${id}`),
      api.get(`/restaurants/${id}/menu`)
    ]).then(([r, m]) => {
      setRestaurant(r.data);
      setItems(m.data);
    }).catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [id]);

  const handleAdd = async (item) => {
    // Warn the user if their cart contains items from a different restaurant
    if (cartRestaurantId && cartRestaurantId !== parseInt(id) && cartItems.length > 0) {
      if (!window.confirm('Adding from a different restaurant will clear your cart. Continue?')) {
        return;
      }
    }
    const result = await addToCart(item.id, 1);
    if (!result.ok) {
      alert(result.message);
    }
  };

  const inCart = (menuItemId) => cartItems.find((i) => i.menuItemId === menuItemId);

  if (loading) return <div className="loading">Loading menu...</div>;
  if (!restaurant) return <div className="empty-state">Restaurant not found</div>;

  // Group items by category
  const grouped = items.reduce((acc, item) => {
    const cat = item.category || 'Other';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(item);
    return acc;
  }, {});

  return (
    <div className="container">
      <button className="btn btn-secondary btn-sm" onClick={() => navigate('/')} style={{ marginBottom: '15px' }}>
        Back to Restaurants
      </button>
      <h2 className="section-title">{restaurant.name}</h2>
      <p style={{ color: '#666', marginBottom: '20px' }}>
        {restaurant.cuisineType} | {restaurant.address}
      </p>

      {items.length === 0 ? (
        <div className="empty-state">No menu items available</div>
      ) : (
        Object.entries(grouped).map(([category, catItems]) => (
          <div key={category} style={{ marginBottom: '30px' }}>
            <h3 style={{ margin: '20px 0 10px', color: '#ff5722' }}>{category}</h3>
            <div className="grid">
              {catItems.map((item) => {
                const cartItem = inCart(item.id);
                return (
                  <div key={item.id} className="card" style={{ cursor: 'default' }}>
                    <img src={item.imageUrl || 'https://via.placeholder.com/280x180?text=Food'} alt={item.name} />
                    <div className="card-body">
                      <h3>{item.name}</h3>
                      <p>{item.description}</p>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '10px' }}>
                        <span className="price">Rs. {item.price}</span>
                        <button className="btn btn-primary btn-sm" onClick={() => handleAdd(item)}>
                          {cartItem ? `Add more (${cartItem.quantity} in cart)` : 'Add to Cart'}
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        ))
      )}
    </div>
  );
}
